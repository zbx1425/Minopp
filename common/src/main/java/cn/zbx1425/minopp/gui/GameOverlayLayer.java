package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
        guiGraphics.drawString(font, tableEntity.state.message, x, y, 0xFFFFFFFF);
        y += font.lineHeight * 2;
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
        guiGraphics.drawString(font, "DEBUG", x, y, 0xFFFFFFFF);
        y += font.lineHeight;
        LocalPlayer player = Minecraft.getInstance().player;
        CardPlayer cardPlayer = ItemHandCards.getCardPlayer(player);
        if (currentPlayer.equals(cardPlayer)) {
            guiGraphics.drawString(font, "It's your turn!", x, y,
                    (System.currentTimeMillis() % 400 < 200) ? 0xFFFFFFFF : 0xFFFFFF00);
        } else {
            guiGraphics.drawString(font, "It's " + currentPlayer.name + "'s turn", x, y, 0xFFFFFFFF);
        }
        y += font.lineHeight;
        guiGraphics.drawString(font, "Phase: " + tableEntity.game.currentPlayerPhase.name(), x, y, 0xFFFFFFFF);
        y += font.lineHeight;
        guiGraphics.drawString(font, "Direction: " + (tableEntity.game.isAntiClockwise ? "ACW" : "CW"), x, y, 0xFFFFFFFF);
        y += font.lineHeight;
        guiGraphics.drawString(font, "Draw Accumulation: " + tableEntity.game.drawCount, x, y, 0xFFFFFFFF);
        y += font.lineHeight * 2;

        guiGraphics.drawString(font, "Top Card: " + tableEntity.game.topCard.getDisplayName().getString(), x, y, tableEntity.game.topCard.suit().color);
        y += font.lineHeight * 2;

        guiGraphics.drawString(font, tableEntity.state.message, x, y, 0xFFFFFFFF);
        y += font.lineHeight;
        if (tableEntity.clientEphemeral != null) {
            guiGraphics.drawString(font, tableEntity.clientEphemeral.message, x, y, 0xFFFFFFFF);
            y += font.lineHeight;
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
                guiGraphics.drawString(font, card.getDisplayName(), x + 5, y + 5, 0xFFDDDDDD);
            }
        }
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
