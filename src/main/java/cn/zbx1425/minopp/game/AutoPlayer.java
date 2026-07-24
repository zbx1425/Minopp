package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.Mino;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AutoPlayer {

    /** Avoid winning, i.e. always not play when on the last card. */
    public boolean noWin;

    /** Avoid playing / passing forward a draw card to a human player, unless impossible due to the force play rule. */
    public boolean noHumanDraw;

    /** Strategy for handling 7/0 cards. */
    public SevenZeroStrategy sevenZeroStrategy;

    /** The possibility that the bot will not shout 'Mino' when it should (0~1). */
    public float forgetChance;

    /** Reduce thinking delay. 0: Norm, 1: Faster, 2: Fastest */
    public byte noDelay;

    /** If the bot can initiate a game. */
    public boolean startGame;

    public enum SevenZeroStrategy {
        /** Try to use 7/0 cards when it can bring itself advantage. */
        SMART,
        /** Treat 7/0 cards like other number cards. */
        RANDOM,
        /** Use a 7/0 cards whenever it's playable. */
        PREFER,
        /** Avoid playing 7/0 cards, unless impossible due to the force play rule. */
        NEVER;

        public static SevenZeroStrategy fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) return SMART;
            return values()[ordinal];
        }
    }

    public AutoPlayer() {
        this(false, false, 0.2f, (byte) 0, false, SevenZeroStrategy.SMART);
    }

    public AutoPlayer(boolean noWin, boolean noHumanDraw, float forgetChance, byte noDelay, boolean startGame, SevenZeroStrategy sevenZeroStrategy) {
        this.noWin = noWin;
        this.noHumanDraw = noHumanDraw;
        this.forgetChance = forgetChance;
        this.noDelay = noDelay;
        this.startGame = startGame;
        this.sevenZeroStrategy = sevenZeroStrategy;
    }

    public AutoPlayer copy() {
        return new AutoPlayer(noWin, noHumanDraw, forgetChance, noDelay, startGame, sevenZeroStrategy);
    }

    private static final Logger LOGGER = Mino.LOGGER;

    public ActionReport playAtGame(CardGame game, CardPlayer realPlayer, MinecraftServer server, TableRuleConfig rules) {
        return playAtGame(game, realPlayer, server, rules, false);
    }

    public ActionReport playAtGame(CardGame game, CardPlayer realPlayer, MinecraftServer server, TableRuleConfig rules, boolean trace) {
        Card topCard = game.topCard;

        CardPlayer nextPlayer = game.players.get(
                (game.currentPlayerIndex + (game.isAntiClockwise ? -1 : 1) + game.players.size()) % game.players.size());
        boolean canPlayDrawCard = !noHumanDraw || server.getPlayerList().getPlayer(nextPlayer.uuid) == null;

        if (trace) {
            LOGGER.warn("AP: Start of Decision");
            LOGGER.warn("AP: Player: {} ({}), Hand size: {}", realPlayer.name, realPlayer.uuid, realPlayer.hand.size());
            LOGGER.warn("AP: Hand: {}", realPlayer.hand);
            LOGGER.warn("AP: TopCard: {}, Phase: {}, DrawCount: {}, ForcePlayCard: {}",
                    topCard, game.currentPlayerPhase, game.drawCount, game.forcePlayCard);
            LOGGER.warn("AP: Strategy: sevenZero={}, noWin={}, noPlayerDraw={}, canPlayDrawCard={}",
                    sevenZeroStrategy, noWin, noHumanDraw, canPlayDrawCard);
            LOGGER.warn("AP: Rules: stacking={}, forcePlay={}, sevenRule={}, zeroRule={}, drawUntilMatch={}",
                    rules.stackingEnabled(), rules.forcePlay(), rules.sevenRuleEnabled(), rules.zeroRuleEnabled(), rules.drawUntilMatch());
        }

        // Phase A: DISCARD_DRAWN + forcePlayCard set
        if (game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN
                && game.forcePlayCard != null) {
            if (trace) LOGGER.warn("AP: Phase A: handleForcePlay");
            return handleForcePlay(game, realPlayer, rules, trace);
        }

        // Phase B: DISCARD_HAND + drawCount > 0
        if (game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_HAND && game.drawCount > 0) {
            if (trace) LOGGER.warn("AP: Phase B: handleDrawStacking");
            return handleDrawStacking(game, realPlayer, rules, canPlayDrawCard, trace);
        }

        // Phase C: Normal play (DISCARD_HAND drawCount==0, or DISCARD_DRAWN without forced forcePlayCard)
        if (trace) LOGGER.warn("AP: Phase C: handleNormalPlay");
        return handleNormalPlay(game, realPlayer, rules, canPlayDrawCard, trace);
    }

    private ActionReport handleForcePlay(CardGame game, CardPlayer realPlayer, TableRuleConfig rules, boolean trace) {
        Card forceCard = game.forcePlayCard;
        Card.Suit wildSuit = pickBestWildColor(realPlayer, forceCard);
        UUID swapTarget = pickSwapTarget(game, realPlayer, forceCard, rules);
        boolean shout = shouldShoutMino(realPlayer, forceCard, game, rules, swapTarget);
        if (trace) {
            LOGGER.warn("AP: ForcePlay: playing {}, wildSuit={}, swapTarget={}, shout={}",
                    forceCard, wildSuit, swapTarget, shout);
        }
        return game.playCard(realPlayer, forceCard, wildSuit, shout, rules, swapTarget);
    }

    private ActionReport handleDrawStacking(CardGame game, CardPlayer realPlayer, TableRuleConfig rules,
                                             boolean canPlayDrawCard, boolean trace) {
        if (!rules.stackingEnabled()) {
            if (trace) LOGGER.warn("AP: DrawStacking: stacking disabled, drawing");
            return game.playNoCard(realPlayer, rules);
        }

        List<Card> stackable = getPlayableCards(game, realPlayer, rules);
        if (trace) LOGGER.warn("AP: DrawStacking: stackable cards: {}", stackable);

        if (noWin && realPlayer.hand.size() == 1) stackable.clear();
        if (!canPlayDrawCard) stackable.clear();

        if (!stackable.isEmpty()) {
            Card card = stackable.get(0);
            Card.Suit wildSuit = pickBestWildColor(realPlayer, card);
            boolean shout = shouldShoutMino(realPlayer, card, game, rules, null);
            if (trace) {
                LOGGER.warn("AP: DrawStacking: stacking {}, wildSuit={}, shout={}", card, wildSuit, shout);
            }
            return game.playCard(realPlayer, card, wildSuit, shout, rules, null);
        }
        if (trace) LOGGER.warn("AP: DrawStacking: no stacking option, drawing {} cards", game.drawCount);
        return game.playNoCard(realPlayer, rules);
    }

    private ActionReport handleNormalPlay(CardGame game, CardPlayer realPlayer, TableRuleConfig rules,
                                           boolean canPlayDrawCard, boolean trace) {
        List<Card> playable = getPlayableCards(game, realPlayer, rules);
        if (trace) LOGGER.warn("AP: NormalPlay: playable cards: {}", playable);

        List<Card> candidates = applyConstraints(playable, realPlayer, rules, canPlayDrawCard);
        if (trace) LOGGER.warn("AP: NormalPlay: after constraints: {}", candidates);

        if (!candidates.isEmpty()) {
            Card bestCard = chooseBestCard(candidates, game, realPlayer, rules, trace);
            Card.Suit wildSuit = pickBestWildColor(realPlayer, bestCard);
            UUID swapTarget = pickSwapTarget(game, realPlayer, bestCard, rules);
            boolean shout = shouldShoutMino(realPlayer, bestCard, game, rules, swapTarget);
            if (trace) {
                LOGGER.warn("AP: NormalPlay: chose {}, wildSuit={}, swapTarget={}, shout={}",
                        bestCard, wildSuit, swapTarget, shout);
            }
            return game.playCard(realPlayer, bestCard, wildSuit, shout, rules, swapTarget);
        }
        if (trace) LOGGER.warn("AP: NormalPlay: no card to play, drawing/passing");
        return game.playNoCard(realPlayer, rules);
    }

    // --- Card filtering ---

    private List<Card> getPlayableCards(CardGame game, CardPlayer player, TableRuleConfig rules) {
        List<Card> result = new ArrayList<>();
        boolean hasNonWD4Playable = false;
        for (Card c : player.hand) {
            if (c.canPlayOn(game.topCard)) {
                result.add(c);
                if (!(c.suit == Card.Suit.WILD && c.family == Card.Family.DRAW)) {
                    hasNonWD4Playable = true;
                }
            }
        }
        if (!rules.wildDrawFourFreeUse() && hasNonWD4Playable) {
            result.removeIf(c -> c.suit == Card.Suit.WILD && c.family == Card.Family.DRAW);
        }
        return result;
    }

    private List<Card> applyConstraints(List<Card> playable, CardPlayer player, TableRuleConfig rules,
                                         boolean canPlayDrawCard) {
        List<Card> result = new ArrayList<>(playable);
        if (noWin && player.hand.size() == 1) {
            result.clear();
            return result;
        }
        if (!canPlayDrawCard) {
            result.removeIf(c -> c.family == Card.Family.DRAW);
        }
        if (sevenZeroStrategy == SevenZeroStrategy.NEVER) {
            if (rules.sevenRuleEnabled()) {
                result.removeIf(c -> c.family == Card.Family.NUMBER && c.number == 7);
            }
            if (rules.zeroRuleEnabled()) {
                result.removeIf(c -> c.family == Card.Family.NUMBER && c.number == 0);
            }
        }
        return result;
    }

    // --- Card scoring ---

    private Card chooseBestCard(List<Card> candidates, CardGame game, CardPlayer realPlayer,
                                 TableRuleConfig rules, boolean trace) {
        Card best = null;
        int bestScore = Integer.MIN_VALUE;
        List<Card> tied = new ArrayList<>();

        for (Card card : candidates) {
            int score = scoreCard(card, game, realPlayer, rules);
            if (trace) LOGGER.warn("AP:   Score {}: {}", card, score);
            if (score > bestScore) {
                bestScore = score;
                best = card;
                tied.clear();
                tied.add(card);
            } else if (score == bestScore) {
                tied.add(card);
            }
        }

        if (tied.size() > 1) {
            Card chosen = tied.get(new Random().nextInt(tied.size()));
            if (trace) LOGGER.warn("AP:   Tied: {}, randomly chose: {}", tied, chosen);
            return chosen;
        }
        return best;
    }

    private int scoreCard(Card card, CardGame game, CardPlayer realPlayer, TableRuleConfig rules) {
        int score = 0;

        if (card.suit == Card.Suit.WILD) {
            score += (card.family == Card.Family.DRAW) ? -10 : 0;
        } else {
            switch (card.family) {
                case SKIP, REVERSE -> score += 20;
                case DRAW -> score += 15;
                case NUMBER -> score += 10;
            }
        }

        if (card.suit != Card.Suit.WILD) {
            int remaining = countSuitInHand(realPlayer.hand, card.suit) - 1;
            score += remaining * 3;
        }

        if (card.family == Card.Family.NUMBER) {
            if (card.number == 7 && rules.sevenRuleEnabled()) {
                switch (sevenZeroStrategy) {
                    case SMART -> score += evaluateSevenSwap(game, realPlayer);
                    case PREFER -> score += 25;
                    default -> {}
                }
            }
            if (card.number == 0 && rules.zeroRuleEnabled()) {
                switch (sevenZeroStrategy) {
                    case SMART -> score += evaluateZeroRotate(game, realPlayer);
                    case PREFER -> score += 25;
                    default -> {}
                }
            }
        }

        return score;
    }

    private int countSuitInHand(List<Card> hand, Card.Suit suit) {
        int count = 0;
        for (Card c : hand) {
            if (c.suit == suit) count++;
        }
        return count;
    }

    private int evaluateSevenSwap(CardGame game, CardPlayer realPlayer) {
        int mySize = realPlayer.hand.size() - 1;
        int minSize = mySize;
        boolean found = false;
        for (CardPlayer p : game.players) {
            if (p.equals(realPlayer)) continue;
            if (p.hand.size() < minSize) {
                minSize = p.hand.size();
                found = true;
            }
        }
        if (!found) return -5;
        return 5 + (mySize - minSize) * 3;
    }

    private int evaluateZeroRotate(CardGame game, CardPlayer realPlayer) {
        int botIndex = game.players.indexOf(realPlayer);
        int n = game.players.size();
        int srcIndex = game.isAntiClockwise
                ? (botIndex + 1) % n
                : Math.floorMod(botIndex - 1, n);
        int srcHandSize = game.players.get(srcIndex).hand.size();
        int mySize = realPlayer.hand.size() - 1;
        int diff = mySize - srcHandSize;
        if (diff > 0) return 5 + diff * 2;
        return -10;
    }

    // --- Auxiliary decisions ---

    private Card.Suit pickBestWildColor(CardPlayer player, Card card) {
        if (card.suit != Card.Suit.WILD) return null;
        int[] counts = new int[4];
        boolean skipped = false;
        for (Card c : player.hand) {
            if (!skipped && c.equals(card)) {
                skipped = true;
                continue;
            }
            if (c.suit != Card.Suit.WILD) {
                counts[c.suit.ordinal()]++;
            }
        }
        int bestIdx = 0;
        for (int i = 1; i < 4; i++) {
            if (counts[i] > counts[bestIdx]) bestIdx = i;
        }
        return Card.Suit.values()[bestIdx];
    }

    private UUID pickSwapTarget(CardGame game, CardPlayer realPlayer, Card card, TableRuleConfig rules) {
        if (card.family != Card.Family.NUMBER || card.number != 7 || !rules.sevenRuleEnabled()) {
            return null;
        }
        switch (sevenZeroStrategy) {
            case SMART -> {
                int mySize = realPlayer.hand.size() - 1;
                CardPlayer bestTarget = null;
                int minSize = mySize;
                for (CardPlayer p : game.players) {
                    if (p.equals(realPlayer)) continue;
                    if (p.hand.size() < minSize) {
                        minSize = p.hand.size();
                        bestTarget = p;
                    }
                }
                return bestTarget != null ? bestTarget.uuid : null;
            }
            case PREFER -> {
                CardPlayer bestTarget = null;
                int minSize = Integer.MAX_VALUE;
                for (CardPlayer p : game.players) {
                    if (p.equals(realPlayer)) continue;
                    if (p.hand.size() < minSize) {
                        minSize = p.hand.size();
                        bestTarget = p;
                    }
                }
                return bestTarget != null ? bestTarget.uuid : null;
            }
            default -> {
                return null;
            }
        }
    }

    private boolean shouldShoutMino(CardPlayer realPlayer, Card card, CardGame game,
                                     TableRuleConfig rules, UUID swapTarget) {
        int predictedSize;
        if (card.family == Card.Family.NUMBER && card.number == 7
                && rules.sevenRuleEnabled() && swapTarget != null) {
            CardPlayer target = game.deAmputate(swapTarget);
            predictedSize = target != null ? target.hand.size() : realPlayer.hand.size() - 1;
        } else if (card.family == Card.Family.NUMBER && card.number == 0
                && rules.zeroRuleEnabled()) {
            int botIndex = game.players.indexOf(realPlayer);
            int n = game.players.size();
            int srcIndex = game.isAntiClockwise
                    ? (botIndex + 1) % n
                    : Math.floorMod(botIndex - 1, n);
            predictedSize = game.players.get(srcIndex).hand.size();
        } else {
            predictedSize = realPlayer.hand.size() - 1;
        }
        if (predictedSize <= 1) {
            return new Random().nextFloat() >= forgetChance;
        }
        return false;
    }

    public static final Codec<AutoPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("NoWin", false).forGetter(a -> a.noWin),
        Codec.BOOL.optionalFieldOf("NoPlayerDraw", false).forGetter(a -> a.noHumanDraw),
        Codec.FLOAT.optionalFieldOf("ForgetChance", 0.2f).forGetter(a -> a.forgetChance),
        Codec.BYTE.optionalFieldOf("NoDelay", (byte) 0).forGetter(a -> a.noDelay),
        Codec.BOOL.optionalFieldOf("StartGame", false).forGetter(a -> a.startGame),
        Codec.INT.optionalFieldOf("SevenZeroStrategy", 0).xmap(SevenZeroStrategy::fromOrdinal, Enum::ordinal)
            .forGetter(a -> a.sevenZeroStrategy)
    ).apply(instance, AutoPlayer::new));
}
