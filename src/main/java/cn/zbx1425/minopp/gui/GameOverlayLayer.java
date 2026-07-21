package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.*;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.platform.multiver.GuiShim;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2FloatArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

//? if <26.1 {
/*import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.FastColor;
*///? } else {

//? }

//? if neoforge
//import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.*;

//? if <26.1
//public class GameOverlayLayer implements LayeredDraw.Layer {
//? if >=26.1 && neoforge
//public class GameOverlayLayer implements GuiLayer {
//? if >=26.1 && !neoforge
public class GameOverlayLayer {

    // Some animation related stuff
    private double zoomAnimationProgress = 0;
    private double zoomAnimationTarget = 0;
    private final Long2FloatArrayMap handCardCurrentXOff = new Long2FloatArrayMap();

    // Newly drawn card highlight tracking
    private final Set<Long> newlyDrawnCardHashes = new HashSet<>();
    private final Map<UUID, Integer> lastSwapGeneration = new HashMap<>();
    private CardGame.PlayerActionPhase lastPhase = null;
    private int lastCurrentPlayerIndex = -1;
    private LongArrayList lastHandCardHashes = new LongArrayList();

    //? if <26.1
    //@Override
    public void render(@NonNull GuiGraphicsExtractor guiGraphics, @NonNull DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos handCardGamePos = ItemHandCards.getHandCardGamePos(player);
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos hitResultGamePos = BlockMinoTable.Client.getCursorPickedGame();
        BlockPos gamePos = (handCardGamePos != null) ? handCardGamePos : hitResultGamePos;
        if (gamePos == null) {
            TurnDeadMan.setOutsideGame();
            MinoClient.handCardOverlayActive = false;
            handCardCurrentXOff.clear();
            return;
        }
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        if (tableEntity == null) {
            TurnDeadMan.setOutsideGame();
            MinoClient.handCardOverlayActive = false;
            handCardCurrentXOff.clear();
            return;
        }

        if (tableEntity.game == null) {
            renderGameInactive(guiGraphics, deltaTracker, tableEntity);
            TurnDeadMan.setOutsideGame();
            zoomAnimationProgress = 0;
            zoomAnimationTarget = 0;
        } else {
            TurnDeadMan.tick(tableEntity.game, deltaTracker);
            if (handCardGamePos == null || hitResultGamePos == null || Objects.equals(handCardGamePos, hitResultGamePos)) {
                // Only render active view when the player is looking at the same table as the hand cards
                renderGameActive(guiGraphics, deltaTracker, tableEntity);
            } else {
                zoomAnimationTarget = 0;
            }
        }
        performZoomAnimation(deltaTracker, tableEntity);
        MinoClient.handCardOverlayActive = renderHandCards(guiGraphics, deltaTracker);
    }

    private void renderGameInactive(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        if (Minecraft.getInstance().options.hideGui) return;
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;
        for (String part : tableEntity.state.message().getString().split("\n")) {
            drawStringWithBackdrop(guiGraphics, font, Component.literal(part), x, y, 0xFFFFFFFF);
            y += font.lineHeight;
        }
        y += font.lineHeight;
        drawStringWithBackdrop(guiGraphics, font, Component.translatable("gui.minopp.play.start_hint"), x, y, 0xFF00DD55);
    }

    private void renderGameActive(GuiGraphicsExtractor g, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        LocalPlayer player = Minecraft.getInstance().player;
        CardPlayer cardPlayer = ItemHandCards.getCardPlayer(player);
        CardPlayer currentPlayer = tableEntity.game.players.get(tableEntity.game.currentPlayerIndex);
        // Zoom effect target
        if (currentPlayer.equals(cardPlayer)) {
            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_HAND) {
                zoomAnimationTarget = 1;
            } else {
                if (zoomAnimationTarget < 1.01) {
                    zoomAnimationTarget = 1.5; // Zoom in first
                } else if (zoomAnimationProgress >= 1.5) { // Already zoomed in
                    zoomAnimationTarget = 1.05; // Zoom out, but not trigger zoom in again
                }
            }
        } else {
            zoomAnimationTarget = 0;
        }

        if (Minecraft.getInstance().options.hideGui) return;
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;
        drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.game_active").append(" © Zbx1425"), x, y, 0xFF7090FF);
        y += font.lineHeight;
        if (currentPlayer.equals(cardPlayer)) {
            drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play." + tableEntity.game.currentPlayerPhase.name().toLowerCase()), x, y,
                    (System.currentTimeMillis() % 400 < 200) ? 0xFFFFFFFF : 0xFFFFFF00);
            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN
                    && tableEntity.rules.forcePlay() && tableEntity.game.lastDrawnCard != null) {
                y += font.lineHeight;
                drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.force_play_hint",
                        tableEntity.game.lastDrawnCard.getDisplayName()), x, y, 0xFFFFAA00);
            }
        } else {
            drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.turn_other", currentPlayer.name), x, y, 0xFFAAAAAA);
        }
        y += font.lineHeight;
        MutableComponent auxInfo = Component.translatable("gui.minopp.play.direction." + (tableEntity.game.isAntiClockwise ? "ccw" : "cw"));
        if (tableEntity.game.drawCount > 0) {
            auxInfo = auxInfo.append(", ").append(Component.translatable("gui.minopp.play.draw_accumulate", tableEntity.game.drawCount));
        }
        drawStringWithBackdrop(g, font, auxInfo, x, y, 0xFFAAAAAA);
        y += font.lineHeight * 2;

        MutableComponent topCardInfo = Component.translatable("gui.minopp.play.top_card", tableEntity.game.topCard.getDisplayName().getString());
        if (tableEntity.game.topCard.suit == Card.Suit.WILD) {
            topCardInfo.append(", ").append(Component.translatable("gui.minopp.play.top_card_wild_color",
                    Component.translatable("game.minopp.card.suit." + tableEntity.game.topCard.getEquivSuit().name().toLowerCase())));
        }
        drawStringWithBackdrop(g, font, topCardInfo, x, y, 0xFFFFFFDD);
        y += font.lineHeight * 2;

        String[] stateLines = tableEntity.state.message().getString().split("\n");
        for (int i = 0; i < stateLines.length; i++) {
            int lineColor = (i == 0) ? 0xFFFFFFFF : 0xFFFFAA00;
            drawStringWithBackdrop(g, font, Component.literal(stateLines[i]), x, y, lineColor);
            y += font.lineHeight;
        }
        for (ListIterator<Pair<ActionMessage, Long>> it = tableEntity.clientMessageList.listIterator(tableEntity.clientMessageList.size()); it.hasPrevious(); ) {
            if (y > Minecraft.getInstance().getWindow().getGuiScaledHeight() - font.lineHeight - 40) {
                break;
            }
            Pair<ActionMessage, Long> entry = it.previous();
            long currentTime = System.currentTimeMillis();
            if (entry.getSecond() - 200 < currentTime) {
                it.remove();
            } else {
                int color = entry.getFirst().type().isEphemeral() ? 0x00FF0000 : 0x00AAAAAA;
                int alpha = Mth.clamp(0 ,0xFF, (int)(0xFF * (entry.getSecond() - currentTime) / 1000));
                drawStringWithBackdrop(g, font, entry.getFirst().message(), x, y, alpha << 24 | color);
                y += font.lineHeight;
            }
        }

        if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) Minecraft.getInstance().hitResult).getBlockPos();
            BlockState hitState = Minecraft.getInstance().level.getBlockState(hitPos);
            if (hitState.is(Mino.BLOCK_MINO_TABLE.get())) {
                boolean isPass = BlockMinoTable.Client.isCursorHittingPile();
                if (currentPlayer.equals(cardPlayer)) {
                    Component cursorMessage = switch (tableEntity.game.currentPlayerPhase) {
                        case DISCARD_HAND -> isPass ? Component.translatable("gui.minopp.play.cursor.pass_draw")
                                : Component.translatable("gui.minopp.play.cursor.play");
                        case DISCARD_DRAWN -> isPass ? Component.translatable("gui.minopp.play.cursor.pass")
                                : Component.translatable("gui.minopp.play.cursor.play");
                    };
                    Component shoutMessage = Component.translatable("gui.minopp.play.cursor.shout");
                    boolean isShouting = !isPass && BlockMinoTable.Client.isShoutModifierHeld();
                    int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                    int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                    boolean highlight = Minecraft.getInstance().level.getGameTime() % 3L < 2L && isPass;
                    int msgWidth = Math.max(font.width(cursorMessage), isShouting ? font.width(shoutMessage) : 0);
                    int msgHeight = isShouting ? font.lineHeight * 2 : font.lineHeight;
                    g.fill(width / 2 + 8, height / 2 - msgHeight / 2 - 2, width / 2 + msgWidth + 16, height / 2 + msgHeight / 2 + 3, highlight ? 0x80AAAA66 : 0x80000000);
                    g.text(font, cursorMessage, width / 2 + 12, height / 2 - msgHeight / 2, highlight ? 0xFF222222 : 0xFFFFFFDD);
                    if (isShouting) {
                        g.text(font, shoutMessage, width / 2 + 12, height / 2 - msgHeight / 2 + font.lineHeight, highlight ? 0xFF222222 : 0xFFFFFFDD);
                    }
                }
            }
        }

        if (TurnDeadMan.isAlarmActive()) {
            Component deadManMessage = Component.translatable("gui.minopp.play.cursor.dead_man");
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            boolean highlight = Minecraft.getInstance().level.getGameTime() % 3L < 2L;
            int msgWidth = font.width(deadManMessage);
            int msgHeight = font.lineHeight;
            GuiShim.pushMatrix(g);
            GuiShim.translate(g, (float)(width / 2), (float)(height / 2 + 12));
            GuiShim.scale(g, 1.5f, 1.5f);
            g.fill(-msgWidth / 2 - 4, 0, msgWidth / 2 + 4, msgHeight + 4, highlight ? 0x80AAAA66 : 0x80000000);
            g.text(font, deadManMessage, -msgWidth / 2, 2, highlight ? 0xFF222222 : 0xFFFFFFDD);
            GuiShim.popMatrix(g);
        }
    }
    
    private static void drawStringWithBackdrop(GuiGraphicsExtractor guiGraphics, Font font, Component component, int x, int y, int color) {
        int i = (int)(0.4 * 255.0F) << 24 & -16777216;
        int var10001 = x - 2;
        int var10002 = y;
        int var10003 = x + font.width(component) + 2;
        guiGraphics.fill(var10001, var10002, var10003, y + font.lineHeight, i);
        guiGraphics.text(font, component, x, y, color, true);
    }

    private static final Identifier ATLAS_LOCATION = Mino.id("textures/gui/deck.png");

    /**
     * Render hand cards on the screen
     * @return whether the hand cards are rendered
     */
    private boolean renderHandCards(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui) return false;

        Font font = Minecraft.getInstance().font;
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos gamePos = ItemHandCards.getHandCardGamePos(player);
        if (gamePos == null) return false;
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        CardPlayer playerWithoutHand = ItemHandCards.getCardPlayer(player);

        final int CARD_V_SPACING = 20;
        final int CARD_WIDTH = (int)(100.0 * Mth.lerp(zoomAnimationProgress, 0.93, 1.0));
        final int CARD_HEIGHT = (int)(CARD_WIDTH * 8.9 / 5.6);

        if (tableEntity.game == null) return false;
        CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
        if (realPlayer == null) return false;
        int clientHandIndex = Mth.clamp(ItemHandCards.getClientHandIndex(player), 0, realPlayer.hand.size() - 1);

        // Compute some hashes for hand cards, for the sake of animation
        realPlayer.hand.sort(Card::compareTo);
        LongArrayList handCardHashes = new LongArrayList();
        for (Card card : realPlayer.hand) {
            if (!handCardHashes.isEmpty() && card.hashCode() == (handCardHashes.getLast() & 0xFFFFFFFFL)) {
                handCardHashes.add(handCardHashes.getLast() + 0x100000000L);
            } else {
                handCardHashes.add(card.hashCode());
            }
        }

        // Track newly drawn cards (after hash computation)
        updateDrawnCardTracking(tableEntity, realPlayer, handCardHashes);

        handCardCurrentXOff.keySet().removeIf(hash -> !handCardHashes.contains(hash));

        //? if <26.1
        //RenderSystem.enableBlend();

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int handSize = realPlayer.hand.size();
        int selectedCardYRaw = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - clientHandIndex));
        int cardDrawOffset = selectedCardYRaw < 20 ? 20 - selectedCardYRaw : 0;
        Random cardRandom = new Random(handSize);
        for (int i = 0; i < handSize; i++) {
            int targetXOff = (i == clientHandIndex ? -30 : 0) + cardRandom.nextInt(-3, 4);
            float currentXOff = handCardCurrentXOff.computeIfAbsent(handCardHashes.getLong(i), ignored -> CARD_WIDTH + 10);
            int x = width - 10 - CARD_WIDTH + (int)currentXOff;
            handCardCurrentXOff.put(handCardHashes.getLong(i),
                    (float)Mth.lerp(8 * 0.05 * deltaTracker.getGameTimeDeltaPartialTick(false),
                            currentXOff, targetXOff));
            int y = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - i)) + cardDrawOffset;
            if (i == clientHandIndex) {
                Card card = realPlayer.hand.get(i);
                Component cardName = card.getDisplayName();
                g.text(font, cardName, x - font.width(cardName) - 10, y + 10, 0xFFFFFFDD);
            }
            boolean isNewlyDrawn = newlyDrawnCardHashes.contains(handCardHashes.getLong(i));
            if (isNewlyDrawn) {
                float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.006) + 1) / 2f;
                int r = (int)(0xFF * (1 - pulse) + 0xCC * pulse);
                int borderColor = 0xFF000000 | (r << 16);
                g.fill(x - 3, y - 3, x + CARD_WIDTH + 3, y + CARD_HEIGHT + 3, borderColor);
            }
            g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF222222);
            g.fill(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, 0xFFDDDDDD);

            Card card = realPlayer.hand.get(i);
            float cardU = switch (card.family) {
                case NUMBER -> Math.abs(card.number) * 16;
                case SKIP -> 160;
                case DRAW -> 176;
                case REVERSE -> 192;
            };
            float cardV = card.suit.ordinal() * 25;
            int cardUW = 16;
            int cardVH = 25;

            float shadowAlpha = (float) Math.max(Mth.lerp(zoomAnimationProgress, 0.5, 0), 0);

//            guiGraphics.fill(x + 3, y + 3, x + CARD_WIDTH - 3, y + CARD_HEIGHT - 3, card.suit.color);
            GuiShim.blit(g,
                ATLAS_LOCATION,
                x + 5, y + 5, CARD_WIDTH - 10, CARD_HEIGHT - 10,
                    cardU + 1, cardV + 1, cardUW - 2, cardVH - 2,
                256, 128);
            GuiShim.pushMatrix(g);
            GuiShim.translate(g, x + 7, y + 7);
            GuiShim.scale(g, 1.5f, 1.5f);
            if (card.family == Card.Family.REVERSE) {
                GuiShim.blit(g, ATLAS_LOCATION, 0, 0, 208, 0, 10, 10, 256, 128);
            } else if (card.family == Card.Family.SKIP) {
                GuiShim.blit(g, ATLAS_LOCATION, 0, 0, 218, 0, 10, 10, 256, 128);
            } else if (card.suit == Card.Suit.WILD && card.family == Card.Family.NUMBER) {
                GuiShim.blit(g, ATLAS_LOCATION, 0, 0, 228, 0, 10, 10, 256, 128);
            } else {
                Component cardName = card.getCardFaceName().copy()
                        .withStyle(Style.EMPTY.withFont(GuiShim.getMiencraftyFontDesc()));
                // blend color with shadowAlpha
                int colorA = (int)(0x22 * shadowAlpha + 0xFF * (1 - shadowAlpha));
                g.text(font, cardName, 0, 0, 0xFF000000 + colorA * 0x10101);
            }

            GuiShim.popMatrix(g);
            GuiShim.pushMatrix(g);
            g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0x222222 | ((int)(0xFF * shadowAlpha) << 24));
            GuiShim.popMatrix(g);
        }

        //? if <26.1
        //RenderSystem.disableBlend();
        
        return true;
    }

    private void updateDrawnCardTracking(BlockEntityMinoTable tableEntity, CardPlayer realPlayer, LongArrayList currentHashes) {
        CardGame game = tableEntity.game;
        if (game == null) {
            newlyDrawnCardHashes.clear();
            lastPhase = null;
            lastCurrentPlayerIndex = -1;
            lastHandCardHashes.clear();
            return;
        }

        // Check swapGeneration change → clear highlights, snapshot current state
        Integer prevGen = lastSwapGeneration.get(realPlayer.uuid);
        if (prevGen != null && prevGen != realPlayer.swapGeneration) {
            newlyDrawnCardHashes.clear();
            lastHandCardHashes = new LongArrayList(currentHashes);
            lastSwapGeneration.put(realPlayer.uuid, realPlayer.swapGeneration);
            lastPhase = game.currentPlayerPhase;
            lastCurrentPlayerIndex = game.currentPlayerIndex;
            return;
        }
        lastSwapGeneration.put(realPlayer.uuid, realPlayer.swapGeneration);

        // Turn/player change → clear highlights
        if (game.currentPlayerIndex != lastCurrentPlayerIndex) {
            newlyDrawnCardHashes.clear();
            lastHandCardHashes = new LongArrayList(currentHashes);
            lastPhase = game.currentPlayerPhase;
            lastCurrentPlayerIndex = game.currentPlayerIndex;
            return;
        }

        CardPlayer currentPlayer = game.players.get(game.currentPlayerIndex);
        boolean isOurTurn = currentPlayer.equals(realPlayer);

        // Detect phase transition DISCARD_HAND → DISCARD_DRAWN (we just drew)
        if (isOurTurn && lastPhase == CardGame.PlayerActionPhase.DISCARD_HAND
                && game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN) {
            // Sorted merge diff to find newly added hashes
            newlyDrawnCardHashes.clear();
            int i = 0, j = 0;
            while (i < currentHashes.size() && j < lastHandCardHashes.size()) {
                long curr = currentHashes.getLong(i);
                long prev = lastHandCardHashes.getLong(j);
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
        }

        lastHandCardHashes = new LongArrayList(currentHashes);
        lastPhase = game.currentPlayerPhase;
        lastCurrentPlayerIndex = game.currentPlayerIndex;
    }

    private void performZoomAnimation(DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        if (Math.abs(zoomAnimationTarget - zoomAnimationProgress) < 0.01) {
            zoomAnimationProgress = zoomAnimationTarget;
        } else {
            zoomAnimationProgress += (zoomAnimationTarget - zoomAnimationProgress) * 8 * 0.05 * deltaTracker.getGameTimeDeltaPartialTick(false);
        }
        MinoClient.globalFovModifier = Mth.lerp(Mth.clamp(zoomAnimationProgress, 0, 1), 1.0, 0.97);
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
