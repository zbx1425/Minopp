package cn.zbx1425.minopp.neoforge.compat.touhou_little_maid.task;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.*;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.neoforge.compat.touhou_little_maid.MemoryTypeRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;

public class PlayMinoTask extends MaidCheckRateTask {

    private CardPlayer cardPlayer;
    private final AutoPlayer autoPlayer = new AutoPlayer();

    public PlayMinoTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        autoPlayer.aiForgetChance = 0;
    }

    @Override
    protected void start(@NotNull ServerLevel level, EntityMaid maid, long gameTimeIn) {
        maid.getBrain().getMemory(MemoryTypeRegister.TARGET_POS.get()).ifPresent(targetPos -> {
            BlockPos tablePos = targetPos.currentBlockPosition();
            BlockState tableState = level.getBlockState(tablePos);
            if (tableState.is(Mino.BLOCK_MINO_TABLE.get())) {
                BlockPos corePos = BlockMinoTable.getCore(tableState, tablePos);
                BlockEntity blockEntity = level.getBlockEntity(corePos);
                if (blockEntity instanceof BlockEntityMinoTable tableEntity) {
                    if (tableEntity.game != null) {
                        inGameLogic(tableEntity, maid);
                        return;
                    }
                    String model = maid.getModelId().split(":")[1];
                    String playerName = maid.hasCustomName() ? maid.getCustomName().getString() : model;
                    CardPlayer cardPlayer = new CardPlayer(maid.getUUID(), playerName);
                    this.cardPlayer = cardPlayer;
                    ItemStack handStack = new ItemStack(Mino.ITEM_HAND_CARDS.get());
                    handStack.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(),
                            new ItemHandCards.CardGameBindingComponent(corePos, cardPlayer.uuid));
                    maid.setItemInHand(InteractionHand.MAIN_HAND, handStack);
                    tableEntity.joinPlayerToTable(cardPlayer, maid.position());
                }
            } else {
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                maid.stopRiding();
                this.cardPlayer = null;
            }
        });
    }
    private void inGameLogic(BlockEntityMinoTable tableEntity, EntityMaid maid) {
        if (tableEntity.game != null) {
            if (tableEntity.game.players.get(tableEntity.game.currentPlayerIndex).equals(cardPlayer)) {
                CardPlayer realPlayer = tableEntity.game.deAmputate(cardPlayer);
                ActionReport result = autoPlayer.playAtGame(tableEntity.game, realPlayer, maid.getServer());
                tableEntity.handleActionResult(result, realPlayer, null);
            }
        }
    }
}