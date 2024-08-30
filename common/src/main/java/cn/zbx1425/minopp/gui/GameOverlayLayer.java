package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.ActionMessage;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Iterator;
import java.util.ListIterator;

public class GameOverlayLayer implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        BlockPos tablePos = null;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            tablePos = player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), ItemHandCards.CardGameBindingComponent.EMPTY)
                    .tablePos().orElse(null);
        }
        HitResult hitResult = Minecraft.getInstance().hitResult;
        ClientLevel level = Minecraft.getInstance().level;
        if (tablePos == null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos potentialTablePos = ((BlockHitResult)hitResult).getBlockPos();
            if (level.getBlockState(potentialTablePos).is(Mino.BLOCK_MINO_TABLE.get())) {
                tablePos = potentialTablePos;
            }
        }
        if (tablePos == null) return;
        BlockState blockState = level.getBlockState(tablePos);
        tablePos = BlockMinoTable.getCore(blockState, tablePos);
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(tablePos);
        if (tableEntity == null) return;

        if (tableEntity.game == null) {
            renderGameInactive(guiGraphics, deltaTracker, tableEntity);
        } else {
            renderGameActive(guiGraphics, deltaTracker, tableEntity);
        }
        renderHandCards(guiGraphics, deltaTracker, tableEntity);
    }

    private void renderGameInactive(GuiGraphics guiGraphics, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        int x = 20, y = 20;
        Font font = Minecraft.getInstance().font;
        for (String part : tableEntity.state.message.getString().split("\n")) {
            guiGraphics.drawString(font, Component.literal(part), x, y, 0xFFFFFFFF);
            y += font.lineHeight;
        }
        y += font.lineHeight;
        guiGraphics.drawString(font, Component.translatable("gui.minopp.play.start_hint"), x, y, 0xFF00DD55);
        y += font.lineHeight * 2;
        guiGraphics.drawString(font, Component.translatable("gui.minopp.play.player_seats"), x, y, 0xFFAAAAAA);
        y += font.lineHeight;
        Direction direction = Direction.NORTH;
        for (int i = 0; i < 4; i++) {
            if (tableEntity.players.get(direction) != null) {
                guiGraphics.drawString(font, direction.getSerializedName() + ": " + tableEntity.players.get(direction).name, x, y, 0xFFFFFFFF);
            } else {
                guiGraphics.drawString(font, direction.getSerializedName() + ": Empty", x, y, 0xFFFFFFFF);
            }
            direction = direction.getClockWise();
            y += font.lineHeight;
        }
    }

    private void renderGameActive(GuiGraphics guiGraphics, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        int x = 20, y = 20;
        Font font = Minecraft.getInstance().font;
        CardPlayer currentPlayer = tableEntity.game.players.get(tableEntity.game.currentPlayer);
        guiGraphics.drawString(font, Component.translatable("gui.minopp.play.game_active"), x, y, 0xFF7090FF);
        y += font.lineHeight;
        LocalPlayer player = Minecraft.getInstance().player;
        CardPlayer cardPlayer = ItemHandCards.getCardPlayer(player);
        if (currentPlayer.equals(cardPlayer)) {
            guiGraphics.drawString(font, Component.translatable("gui.minopp.play." + tableEntity.game.currentPlayerPhase.name().toLowerCase()), x, y,
                    (System.currentTimeMillis() % 400 < 200) ? 0xFFFFFFFF : 0xFFFFFF00);
        } else {
            guiGraphics.drawString(font, Component.translatable("gui.minopp.play.turn_other", currentPlayer.name), x, y, 0xFFAAAAAA);
        }
        y += font.lineHeight;
        MutableComponent auxInfo = Component.translatable("gui.minopp.play.direction." + (tableEntity.game.isAntiClockwise ? "ccw" : "cw"));
        if (tableEntity.game.drawCount > 0) {
            auxInfo = auxInfo.append(", ").append(Component.translatable("gui.minopp.play.draw_accumulate", tableEntity.game.drawCount));
        }
        guiGraphics.drawString(font, auxInfo, x, y, 0xFFAAAAAA);
        y += font.lineHeight * 2;

        MutableComponent topCardInfo = Component.translatable("gui.minopp.play.top_card", tableEntity.game.topCard.getDisplayName().getString());
        if (tableEntity.game.topCard.getActualCard().suit() == Card.Suit.WILD) {
            topCardInfo.append(", ").append(Component.translatable("gui.minopp.play.top_card_wild_color",
                    Component.translatable("game.minopp.card.suit." + tableEntity.game.topCard.suit().name().toLowerCase())));
        }
        guiGraphics.drawString(font, topCardInfo, x, y, 0xFFFFFFDD);
        y += font.lineHeight * 2;

        for (String part : tableEntity.state.message.getString().split("\n")) {
            guiGraphics.drawString(font, Component.literal(part), x, y, 0xFFFFFFFF);
            y += font.lineHeight;
        }
        for (ListIterator<Pair<ActionMessage, Long>> it = tableEntity.clientMessageList.listIterator(tableEntity.clientMessageList.size()); it.hasPrevious(); ) {
            Pair<ActionMessage, Long> entry = it.previous();
            long currentTime = System.currentTimeMillis();
            if (entry.getSecond() - 200 < currentTime) {
                it.remove();
            } else {
                int color = entry.getFirst().isEphemeral ? 0x00FF0000 : 0x00AAAAAA;
                int alpha = Mth.clamp(0 ,0xFF, (int)(0xFF * (entry.getSecond() - currentTime) / 1000));
                guiGraphics.drawString(font, entry.getFirst().message, x, y, alpha << 24 | color);
                y += font.lineHeight;
            }
        }
    }

    private void renderHandCards(GuiGraphics guiGraphics, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        Font font = Minecraft.getInstance().font;
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos tablePos = null;
        int clientHandIndex = 0;
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            tablePos = player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), ItemHandCards.CardGameBindingComponent.EMPTY)
                    .tablePos().orElse(null);
            clientHandIndex = player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), 0);
        }
//        if (tablePos == null) return;
//        BlockState blockState = level.getBlockState(tablePos);
//        tablePos = BlockMinoTable.getCore(blockState, tablePos);
//        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(tablePos);
        CardPlayer playerWithoutHand = ItemHandCards.getCardPlayer(player);

        final int CARD_V_SPACING = 20;
        final int CARD_WIDTH = 100;
        final int CARD_HEIGHT = (int)(CARD_WIDTH * 8.9 / 5.6);

        if (tableEntity.game == null) return;
        CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
        if (realPlayer == null) return;
        if (clientHandIndex > realPlayer.hand.size()) {
            clientHandIndex = realPlayer.hand.size();
            player.getMainHandItem().set(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), clientHandIndex);
        }

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int handSize = realPlayer.hand.size();
        int selectedCardYRaw = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - clientHandIndex));
        int cardDrawOffset = selectedCardYRaw < 20 ? 20 - selectedCardYRaw : 0;
        for (int i = 0; i <= handSize; i++) {
            int x = width - 20 - CARD_WIDTH - (i == clientHandIndex ? 20 : 0);
            int y = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - i)) + cardDrawOffset;
            guiGraphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF222222);
            guiGraphics.fill(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, 0xFFDDDDDD);
            if (i == handSize) {
                guiGraphics.fill(x + 3, y + 3, x + CARD_WIDTH - 3, y + CARD_HEIGHT - 3, 0xFF555555);
                guiGraphics.drawString(font, "Pass", x + 5, y + 5, 0xFFDDDDDD);
            } else {
                Card card = realPlayer.hand.get(i);
                guiGraphics.fill(x + 3, y + 3, x + CARD_WIDTH - 3, y + CARD_HEIGHT - 3, card.suit().color);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x + 5, y + 5, 0);
                guiGraphics.pose().scale(1.5f, 1.5f, 0);
                guiGraphics.drawString(font, card.getCardFaceName(), 0, 0, 0xFFDDDDDD);
                guiGraphics.pose().popPose();
            }
        }
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
