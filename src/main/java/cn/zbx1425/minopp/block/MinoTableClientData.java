package cn.zbx1425.minopp.block;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.game.shard.ActionReportShard;
import cn.zbx1425.minopp.game.shard.GameWonShard;
import cn.zbx1425.minopp.game.shard.PlayShard;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2FloatArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MinoTableClientData {

    // ========== Ephemeral Shards (existing) ==========

    private final List<Pair<ActionReportShard, Long>> ephemeralShards = new ArrayList<>();
    private int lastRoundId = -1;
    @Nullable
    private UUID lastTopCardPlayer = null;
    @Nullable
    private CardGame lastGame = null;
    private List<ActionReportShard> lastStateShards = List.of();

    // ========== Zoom Animation ==========

    private double zoomProgress = 0;
    private double zoomTarget = 0;

    // ========== Per-Card X Offset ==========

    private final Long2FloatArrayMap handCardXOff = new Long2FloatArrayMap();

    // ========== Newly Drawn Card Tracking ==========

    private final Set<Long> newlyDrawnCardHashes = new HashSet<>();
    private int lastLocalSwapGeneration = -1;
    @Nullable
    private CardGame.PlayerActionPhase lastLocalPhase = null;
    private int lastLocalRoundId = -1;
    private List<Card> lastLocalHand = new ArrayList<>();

    // ========== Swap Animation Queue ==========

    public enum SwapPhase { IDLE, FLY_OUT, FLY_IN }

    public record SwapAnimationEntry(List<Card> oldHand, List<Card> newHand) {}

    private final Deque<SwapAnimationEntry> swapQueue = new ArrayDeque<>();
    private SwapPhase currentSwapPhase = SwapPhase.IDLE;
    private long swapPhaseStartTime = 0;
    private long lastTickTime = 0;

    private static final long FLY_OUT_DURATION_MS = 350;
    private static final long FLY_IN_DURATION_MS = 450;
    private static final long STALE_THRESHOLD_MS = 2000;

    // ========== Render State ==========

    public record HandRenderState(
        List<Card> cards,
        LongArrayList cardHashes,
        double zoomProgress,
        float yOffset,
        float spacingMultiplier,
        int selectedIndex,
        Set<Long> highlightHashes,
        boolean useXOffAnimation
    ) {}

    // ========== Tick ==========

    public void tickAnimations(float deltaPartialTick) {
        long now = System.currentTimeMillis();
        if (lastTickTime > 0 && now - lastTickTime > STALE_THRESHOLD_MS) {
            snapAllToTarget();
        }
        lastTickTime = now;
        advanceZoom(deltaPartialTick);
        advanceSwapQueue(now);
    }

    private void snapAllToTarget() {
        zoomProgress = zoomTarget;
        swapQueue.clear();
        currentSwapPhase = SwapPhase.IDLE;
    }

    private void advanceZoom(float deltaPartialTick) {
        if (Math.abs(zoomTarget - zoomProgress) < 0.01) {
            zoomProgress = zoomTarget;
        } else {
            zoomProgress += (zoomTarget - zoomProgress) * 8 * 0.05 * deltaPartialTick;
        }
    }

    private void advanceSwapQueue(long now) {
        if (currentSwapPhase == SwapPhase.IDLE) return;

        long elapsed = now - swapPhaseStartTime;
        if (currentSwapPhase == SwapPhase.FLY_OUT) {
            if (elapsed >= FLY_OUT_DURATION_MS) {
                currentSwapPhase = SwapPhase.FLY_IN;
                swapPhaseStartTime = now;
            }
        } else if (currentSwapPhase == SwapPhase.FLY_IN) {
            if (elapsed >= FLY_IN_DURATION_MS) {
                swapQueue.poll();
                if (!swapQueue.isEmpty()) {
                    currentSwapPhase = SwapPhase.FLY_OUT;
                    swapPhaseStartTime = now;
                } else {
                    currentSwapPhase = SwapPhase.IDLE;
                }
            }
        }
    }

    // ========== Zoom Accessors ==========

    public void setZoomTarget(double target) {
        this.zoomTarget = target;
    }

    public double getZoomProgress() {
        return zoomProgress;
    }

    public double getZoomTarget() {
        return zoomTarget;
    }

    // ========== X Offset ==========

    public Long2FloatArrayMap getHandCardXOff() {
        return handCardXOff;
    }

    // ========== Newly Drawn ==========

    public Set<Long> getNewlyDrawnCardHashes() {
        return newlyDrawnCardHashes;
    }

    // ========== Swap State Queries ==========

    public SwapPhase getCurrentSwapPhase() {
        return currentSwapPhase;
    }

    public boolean isSwapAnimating() {
        return currentSwapPhase != SwapPhase.IDLE;
    }

    // ========== Render State Computation ==========

    /**
     * @param realPlayer must have hand already sorted in-place by caller
     */
    public HandRenderState getHandRenderState(CardPlayer realPlayer, int clientHandIndex) {
        long now = System.currentTimeMillis();

        if (currentSwapPhase == SwapPhase.IDLE || swapQueue.isEmpty()) {
            LongArrayList hashes = computeCardHashes(realPlayer.hand);
            return new HandRenderState(
                realPlayer.hand, hashes, zoomProgress,
                0f, 1f,
                clientHandIndex, newlyDrawnCardHashes, true
            );
        }

        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        SwapAnimationEntry current = swapQueue.peek();
        boolean isLastEntry = swapQueue.size() == 1;
        long elapsed = now - swapPhaseStartTime;

        if (currentSwapPhase == SwapPhase.FLY_OUT) {
            float t = Mth.clamp((float) elapsed / FLY_OUT_DURATION_MS, 0f, 1f);
            float easedT = easeIn(t);
            float spacingMul = Mth.lerp(easedT, 1f, END_SPACING_MUL);
            float flyOutDistance = computeFlyDistance(screenHeight, current.oldHand().size(), 0);
            float yOffset = -easedT * flyOutDistance;

            List<Card> cards = new ArrayList<>(current.oldHand());
            cards.sort(Card::compareTo);
            LongArrayList hashes = computeCardHashes(cards);
            return new HandRenderState(
                cards, hashes, 0,
                yOffset, spacingMul,
                -1, Set.of(), false
            );
        } else {
            float t = Mth.clamp((float) elapsed / FLY_IN_DURATION_MS, 0f, 1f);
            float easedT = easeOut(t);
            float spacingMul = Mth.lerp(easedT, END_SPACING_MUL, 1f);

            if (isLastEntry) {
                float flyInDistance = computeFlyDistance(screenHeight, realPlayer.hand.size(), zoomProgress);
                float yOffset = Mth.lerp(easedT, flyInDistance, 0f);
                LongArrayList hashes = computeCardHashes(realPlayer.hand);
                return new HandRenderState(
                    realPlayer.hand, hashes, zoomProgress,
                    yOffset, spacingMul,
                    clientHandIndex, newlyDrawnCardHashes, true
                );
            } else {
                float flyInDistance = computeFlyDistance(screenHeight, current.newHand().size(), 0);
                float yOffset = Mth.lerp(easedT, flyInDistance, 0f);
                List<Card> cards = new ArrayList<>(current.newHand());
                cards.sort(Card::compareTo);
                LongArrayList hashes = computeCardHashes(cards);
                return new HandRenderState(
                    cards, hashes, 0,
                    yOffset, spacingMul,
                    -1, Set.of(), false
                );
            }
        }
    }

    // ========== Detection Logic (called from onBlockEntitySync) ==========

    private void updateLocalPlayerTracking(@Nullable CardGame currentGame) {
        if (currentGame == null) {
            newlyDrawnCardHashes.clear();
            lastLocalPhase = null;
            lastLocalRoundId = -1;
            lastLocalSwapGeneration = -1;
            lastLocalHand.clear();
            swapQueue.clear();
            currentSwapPhase = SwapPhase.IDLE;
            return;
        }

        UUID localPlayerUuid = Minecraft.getInstance().player != null
                ? PlayerShim.getGameProfileId(Minecraft.getInstance().player) : null;
        if (localPlayerUuid == null) return;

        CardPlayer localPlayer = null;
        for (CardPlayer p : currentGame.players) {
            if (p.uuid.equals(localPlayerUuid)) {
                localPlayer = p;
                break;
            }
        }
        if (localPlayer == null) return;

        // Detect swap
        if (lastLocalSwapGeneration != localPlayer.swapGeneration) {
            if (lastLocalSwapGeneration != -1 && !lastLocalHand.isEmpty()) {
                swapQueue.add(new SwapAnimationEntry(
                    List.copyOf(lastLocalHand),
                    List.copyOf(localPlayer.hand)
                ));
                if (currentSwapPhase == SwapPhase.IDLE) {
                    currentSwapPhase = SwapPhase.FLY_OUT;
                    swapPhaseStartTime = System.currentTimeMillis();
                }
            }
            newlyDrawnCardHashes.clear();
            lastLocalHand = new ArrayList<>(localPlayer.hand);
            lastLocalSwapGeneration = localPlayer.swapGeneration;
            lastLocalPhase = currentGame.currentPlayerPhase;
            lastLocalRoundId = currentGame.roundId;
            return;
        }

        // Detect newly drawn cards
        if (currentGame.roundId != lastLocalRoundId) {
            CardPlayer currentPlayer = currentGame.players.get(currentGame.currentPlayerIndex);
            boolean isOurTurn = currentPlayer.equals(localPlayer);

            if (isOurTurn && lastLocalPhase == CardGame.PlayerActionPhase.DISCARD_HAND
                    && currentGame.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN) {
                newlyDrawnCardHashes.clear();
                List<Card> sortedCurrent = new ArrayList<>(localPlayer.hand);
                sortedCurrent.sort(Card::compareTo);
                LongArrayList currentHashes = computeCardHashes(sortedCurrent);

                List<Card> sortedLast = new ArrayList<>(lastLocalHand);
                sortedLast.sort(Card::compareTo);
                LongArrayList lastHashes = computeCardHashes(sortedLast);

                int i = 0, j = 0;
                while (i < currentHashes.size() && j < lastHashes.size()) {
                    long curr = currentHashes.getLong(i);
                    long prev = lastHashes.getLong(j);
                    if (curr == prev) {
                        i++; j++;
                    } else if (curr < prev) {
                        newlyDrawnCardHashes.add(curr);
                        i++;
                    } else {
                        j++;
                    }
                }
                while (i < currentHashes.size()) {
                    newlyDrawnCardHashes.add(currentHashes.getLong(i++));
                }
            } else {
                newlyDrawnCardHashes.clear();
            }

            lastLocalHand = new ArrayList<>(localPlayer.hand);
            lastLocalPhase = currentGame.currentPlayerPhase;
            lastLocalRoundId = currentGame.roundId;
            return;
        }

        lastLocalHand = new ArrayList<>(localPlayer.hand);
        lastLocalPhase = currentGame.currentPlayerPhase;
    }

    // ========== onBlockEntitySync ==========

    public void onBlockEntitySync(@Nullable CardGame currentGame, List<ActionReportShard> currentShards) {
        CardGame oldGame = this.lastGame;
        List<ActionReportShard> oldShards = this.lastStateShards;

        this.lastGame = currentGame;
        this.lastStateShards = List.copyOf(currentShards);

        if (oldGame == null && currentGame != null) {
            ephemeralShards.clear();
            lastRoundId = currentGame.roundId;
            lastTopCardPlayer = currentGame.topCardPlayer;
            updateLocalPlayerTracking(currentGame);
            return;
        }

        if (oldGame != null && currentGame == null) {
            boolean hasGameWon = currentShards.stream().anyMatch(s -> s instanceof GameWonShard);
            if (hasGameWon) {
                long now = System.currentTimeMillis();
                for (ActionReportShard old : oldShards) {
                    if (old.isNoteworthy()) {
                        ephemeralShards.add(new Pair<>(old, now));
                    }
                }
            } else {
                ephemeralShards.clear();
            }
            lastRoundId = -1;
            lastTopCardPlayer = null;
            updateLocalPlayerTracking(null);
            return;
        }

        if (currentGame == null) return;
        if (currentGame.roundId == lastRoundId) {
            updateLocalPlayerTracking(currentGame);
            return;
        }

        long now = System.currentTimeMillis();
        boolean newStateHasPlay = currentShards.stream().anyMatch(s -> s instanceof PlayShard);

        if (newStateHasPlay && lastTopCardPlayer != null) {
            PlayShard oldPlay = findPlayShard(oldShards);
            if (oldPlay == null && oldGame != null) {
                oldPlay = new PlayShard(lastTopCardPlayer, oldGame.topCard, false);
            }
            if (oldPlay != null) {
                ephemeralShards.add(new Pair<>(oldPlay, now));
            }
        }

        for (ActionReportShard old : oldShards) {
            if (old.isNoteworthy()) {
                ephemeralShards.add(new Pair<>(old, now));
            }
        }

        ephemeralShards.removeIf(e ->
                e.getFirst().shardType().lifecycle() == ActionReportShard.Lifecycle.REJECTION);

        lastRoundId = currentGame.roundId;
        lastTopCardPlayer = currentGame.topCardPlayer;

        updateLocalPlayerTracking(currentGame);
    }

    // ========== Existing Public Methods ==========

    public void addEphemeral(ActionReportShard shard) {
        ephemeralShards.add(new Pair<>(shard, System.currentTimeMillis()));
    }

    public List<Pair<ActionReportShard, Long>> getEphemeralShards() {
        return ephemeralShards;
    }

    // ========== Utility ==========

    public static LongArrayList computeCardHashes(List<Card> sortedCards) {
        LongArrayList hashes = new LongArrayList();
        for (Card card : sortedCards) {
            if (!hashes.isEmpty() && card.hashCode() == (hashes.getLong(hashes.size() - 1) & 0xFFFFFFFFL)) {
                hashes.add(hashes.getLong(hashes.size() - 1) + 0x100000000L);
            } else {
                hashes.add(card.hashCode());
            }
        }
        return hashes;
    }

    /**
     * Computes the Y distance needed to move all cards fully off-screen.
     */
    private static float computeFlyDistance(int screenHeight, int handSize, double zoomProg) {
        int cardWidth = (int)(100.0 * Mth.lerp(zoomProg, 0.93, 1.0));
        int cardHeight = (int)(cardWidth * 8.9 / 5.6);
        float endSpacing = 20f * END_SPACING_MUL;
        float stackExtent = cardHeight / 2f + endSpacing * handSize;
        return Math.max(screenHeight + cardHeight / 2f, stackExtent + cardHeight) + 20f;
    }

    private static final float END_SPACING_MUL = 0.3f;

    private static float easeIn(float t) {
        return t * t;
    }

    private static float easeOut(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    @Nullable
    private static PlayShard findPlayShard(List<ActionReportShard> shards) {
        for (ActionReportShard s : shards) {
            if (s instanceof PlayShard ps) return ps;
        }
        return null;
    }
}
