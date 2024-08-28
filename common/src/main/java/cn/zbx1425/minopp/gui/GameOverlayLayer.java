package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
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

        int x = 20, y = 20;
        Font font = Minecraft.getInstance().font;
        if (tableEntity.game == null) {
            guiGraphics.drawString(font, "Game Inactive", x, y, 0xFFFFFFFF);
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
        } else {
            CardPlayer currentPlayer = tableEntity.game.players.get(tableEntity.game.currentPlayer);
            guiGraphics.drawString(font, "Current Player: " + currentPlayer.name, x, y, 0xFFFFFFFF);
            y += font.lineHeight;
            guiGraphics.drawString(font, "State: " + tableEntity.state.message.getString(), x, y, 0xFFFFFFFF);
        }
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
