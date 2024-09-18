package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.ActionMessage;
import cn.zbx1425.minopp.game.ActionReport;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;

public class GameOverlayLayer implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos handCardGamePos = ItemHandCards.getHandCardGamePos(player);
        HitResult hitResult = Minecraft.getInstance().hitResult;
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos hitResultGamePos = null;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos potentialTablePos = ((BlockHitResult)hitResult).getBlockPos();
            if (level.getBlockState(potentialTablePos).is(Mino.BLOCK_MINO_TABLE.get())) {
                hitResultGamePos = BlockMinoTable.getCore(level.getBlockState(potentialTablePos), potentialTablePos);
            }
        }
        BlockPos gamePos = (handCardGamePos != null) ? handCardGamePos : hitResultGamePos;
        if (gamePos == null) return;
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        if (tableEntity == null) return;

        if (tableEntity.game == null) {
            renderGameInactive(guiGraphics, deltaTracker, tableEntity);
        } else if (handCardGamePos == null || hitResultGamePos == null || Objects.equals(handCardGamePos, hitResultGamePos)) {
            // Only render active view when the player is looking at the same table as the hand cards
            renderGameActive(guiGraphics, deltaTracker, tableEntity);
        }
        renderHandCards(guiGraphics, deltaTracker);
    }

    private void renderGameInactive(GuiGraphics guiGraphics, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;
        for (String part : tableEntity.state.message().getString().split("\n")) {
            drawStringWithBackdrop(guiGraphics, font, Component.literal(part), x, y, 0xFFFFFFFF);
            y += font.lineHeight;
        }
        y += font.lineHeight;
        drawStringWithBackdrop(guiGraphics, font, Component.translatable("gui.minopp.play.start_hint"), x, y, 0xFF00DD55);
    }

    private void renderGameActive(GuiGraphics guiGraphics, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;
        CardPlayer currentPlayer = tableEntity.game.players.get(tableEntity.game.currentPlayerIndex);
        drawStringWithBackdrop(guiGraphics, font, Component.translatable("gui.minopp.play.game_active").append(" © Zbx1425"), x, y, 0xFF7090FF);
        y += font.lineHeight;
        LocalPlayer player = Minecraft.getInstance().player;
        CardPlayer cardPlayer = ItemHandCards.getCardPlayer(player);
        if (currentPlayer.equals(cardPlayer)) {
            drawStringWithBackdrop(guiGraphics, font, Component.translatable("gui.minopp.play." + tableEntity.game.currentPlayerPhase.name().toLowerCase()), x, y,
                    (System.currentTimeMillis() % 400 < 200) ? 0xFFFFFFFF : 0xFFFFFF00);
        } else {
            drawStringWithBackdrop(guiGraphics, font, Component.translatable("gui.minopp.play.turn_other", currentPlayer.name), x, y, 0xFFAAAAAA);
        }
        y += font.lineHeight;
        MutableComponent auxInfo = Component.translatable("gui.minopp.play.direction." + (tableEntity.game.isAntiClockwise ? "ccw" : "cw"));
        if (tableEntity.game.drawCount > 0) {
            auxInfo = auxInfo.append(", ").append(Component.translatable("gui.minopp.play.draw_accumulate", tableEntity.game.drawCount));
        }
        drawStringWithBackdrop(guiGraphics, font, auxInfo, x, y, 0xFFAAAAAA);
        y += font.lineHeight * 2;

        MutableComponent topCardInfo = Component.translatable("gui.minopp.play.top_card", tableEntity.game.topCard.getDisplayName().getString());
        if (tableEntity.game.topCard.suit == Card.Suit.WILD) {
            topCardInfo.append(", ").append(Component.translatable("gui.minopp.play.top_card_wild_color",
                    Component.translatable("game.minopp.card.suit." + tableEntity.game.topCard.getEquivSuit().name().toLowerCase())));
        }
        drawStringWithBackdrop(guiGraphics, font, topCardInfo, x, y, 0xFFFFFFDD);
        y += font.lineHeight * 2;

        for (String part : tableEntity.state.message().getString().split("\n")) {
            drawStringWithBackdrop(guiGraphics, font, Component.literal(part), x, y, 0xFFFFFFFF);
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
                drawStringWithBackdrop(guiGraphics, font, entry.getFirst().message(), x, y, alpha << 24 | color);
                y += font.lineHeight;
            }
        }

        if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) Minecraft.getInstance().hitResult).getBlockPos();
            BlockState hitState = Minecraft.getInstance().level.getBlockState(hitPos);
            if (hitState.is(Mino.BLOCK_MINO_TABLE.get())) {
                boolean isPass = hitState.getValue(BlockMinoTable.PART) == BlockMinoTable.TablePartType.X_LESS_Z_LESS;
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
                    guiGraphics.fill(width / 2 + 8, height / 2 - msgHeight / 2 - 2, width / 2 + msgWidth + 16, height / 2 + msgHeight / 2 + 2, highlight ? 0x80AAAA66 : 0x80000000);
                    drawStringWithBackdrop(guiGraphics, font, cursorMessage, width / 2 + 12, height / 2 - msgHeight / 2, highlight ? 0xFF222222 : 0xFFFFFFDD);
                    if (isShouting) {
                        drawStringWithBackdrop(guiGraphics, font, shoutMessage, width / 2 + 12, height / 2 - msgHeight / 2 + font.lineHeight, highlight ? 0xFF222222 : 0xFFFFFFDD);
                    }
                }
            }
        }
    }
    
    private static void drawStringWithBackdrop(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color) {
        int i = (int)(0.4 * 255.0F) << 24 & -16777216;
        int var10001 = x - 2;
        int var10002 = y ;
        int var10003 = x + font.width(component) + 2;
        guiGraphics.fill(var10001, var10002, var10003, y + font.lineHeight, FastColor.ARGB32.multiply(i, color));
        guiGraphics.drawString(font, component, x, y, color, true);
    }

    private static final ResourceLocation ATLAS_LOCATION = Mino.id("textures/gui/deck.png");

    private void renderHandCards(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Font font = Minecraft.getInstance().font;
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos gamePos = ItemHandCards.getHandCardGamePos(player);
        if (gamePos == null) return;
        int clientHandIndex = ItemHandCards.getClientHandIndex(player);
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        CardPlayer playerWithoutHand = ItemHandCards.getCardPlayer(player);

        final int CARD_V_SPACING = 20;
        final int CARD_WIDTH = 100;
        final int CARD_HEIGHT = (int)(CARD_WIDTH * 8.9 / 5.6);

        if (tableEntity.game == null) return;
        CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
        if (realPlayer == null) return;
        if (clientHandIndex > realPlayer.hand.size() - 1) {
            clientHandIndex = realPlayer.hand.size() - 1;
            player.getMainHandItem().set(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), clientHandIndex);
        }

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int handSize = realPlayer.hand.size();
        int selectedCardYRaw = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - clientHandIndex));
        int cardDrawOffset = selectedCardYRaw < 20 ? 20 - selectedCardYRaw : 0;
        Random cardRandom = new Random(handSize);
        for (int i = 0; i < handSize; i++) {
            int x = width - 20 - CARD_WIDTH - (i == clientHandIndex ? 30 : 0) + cardRandom.nextInt(-3, 4);
            int y = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - i)) + cardDrawOffset;
            if (i == clientHandIndex) {
                Card card = realPlayer.hand.get(i);
                Component cardName = card.getDisplayName();
                guiGraphics.drawString(font, cardName, x - font.width(cardName) - 10, y + 10, 0xFFFFFFDD);
            }
            guiGraphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF222222);
            guiGraphics.fill(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, 0xFFDDDDDD);

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
//            guiGraphics.fill(x + 3, y + 3, x + CARD_WIDTH - 3, y + CARD_HEIGHT - 3, card.suit.color);
            guiGraphics.blit(ATLAS_LOCATION, x + 5, y + 5, CARD_WIDTH - 10, CARD_HEIGHT - 10,
                    cardU + 1, cardV + 1, cardUW - 2, cardVH - 2, 256, 128);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 7, y + 7, 0);
            guiGraphics.pose().scale(1.5f, 1.5f, 0);
            if (card.family == Card.Family.REVERSE) {
                guiGraphics.blit(ATLAS_LOCATION, 0, 0, 208, 0, 10, 10, 256, 128);
            } else if (card.family == Card.Family.SKIP) {
                guiGraphics.blit(ATLAS_LOCATION, 0, 0, 218, 0, 10, 10, 256, 128);
            } else if (card.suit == Card.Suit.WILD && card.family == Card.Family.NUMBER) {
                guiGraphics.blit(ATLAS_LOCATION, 0, 0, 228, 0, 10, 10, 256, 128);
            } else {
                guiGraphics.drawString(font, card.getCardFaceName(), 0, 0, 0xFFFFFFFF);
            }
            guiGraphics.pose().popPose();
        }
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
