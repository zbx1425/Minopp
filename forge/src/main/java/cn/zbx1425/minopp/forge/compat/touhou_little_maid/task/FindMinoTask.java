package cn.zbx1425.minopp.forge.compat.touhou_little_maid.task;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.forge.compat.touhou_little_maid.entity.MaidEntitySit;
import cn.zbx1425.minopp.forge.compat.touhou_little_maid.MemoryTypeRegister;
import cn.zbx1425.minopp.forge.compat.touhou_little_maid.PoiRegistry;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class FindMinoTask extends MaidCheckRateTask {
    public static final String MinoTable = "mino_table";
    float speed;
    int closeEnoughDist;

    public FindMinoTask(float movementSpeed, int closeEnoughDist) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        this.closeEnoughDist = closeEnoughDist;
        this.speed = movementSpeed;
        this.setMaxCheckRate(10);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (super.checkExtraStartConditions(worldIn, maid) && maid.canBrainMoving() && (maid.getMainHandItem().isEmpty() || maid.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get()))) {
            BlockPos seatPos = findSeat(worldIn, maid);
            if (seatPos != null && maid.isWithinRestriction(seatPos)) {
                if (seatPos.distToCenterSqr(maid.position()) < Math.pow(this.closeEnoughDist, 2)) {
                    maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(seatPos));
                    return true;
                }
                BehaviorUtils.setWalkAndLookTargetMemories(maid, seatPos, speed, 1);
                this.setNextCheckTickCount(5);
            } else {
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            }
        }
        return false;
    }

    @Override
    protected void start(@NotNull ServerLevel level, EntityMaid maid, long gameTimeIn) {
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent((targetPos) -> {
            BlockPos pos = targetPos.currentBlockPosition();
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() instanceof BlockMinoTable minoTable) {
                this.startMaidSit(maid, blockState, level, pos);
                maid.getBrain().setMemory(MemoryTypeRegister.TARGET_POS.get(), targetPos);
            }
        });
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Nullable
    private BlockPos findSeat(ServerLevel world, EntityMaid maid) {
        BlockPos blockPos = maid.getBrainSearchPos();
        PoiManager poiManager = world.getPoiManager();
        int range = (int) maid.getRestrictRadius();
        return poiManager.getInRange(type -> type.value().equals(PoiRegistry.MINO_TABLE.get()), blockPos, range, PoiManager.Occupancy.ANY)
                .map(poiRecord -> {
                    BlockPos pos = poiRecord.getPos();
                    BlockState state = world.getBlockState(pos);
                    return BlockMinoTable.getCore(state, pos);
                })
                .filter(pos -> !isOccupied(world, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition()))).orElse(null);
    }

    public void startMaidSit(EntityMaid maid, BlockState state, Level worldIn, BlockPos pos) {
        if (worldIn instanceof ServerLevel serverLevel && worldIn.getBlockEntity(pos) instanceof BlockEntityMinoTable minoTable) {
            List<Direction> emptyDirections = minoTable.getEmptyDirections();
            if (!emptyDirections.isEmpty()) {
                Random random = new Random();
                Direction direction = emptyDirections.get(random.nextInt(emptyDirections.size()));
                Vec3i position = direction.getNormal().multiply(2).offset(1, 0, 1);
                MaidEntitySit newSitEntity = new MaidEntitySit(worldIn, Vec3.atLowerCornerWithOffset(pos, position.getX(), position.getY(), position.getZ()), MinoTable, pos.offset(1, 0, 1));
                newSitEntity.setYRot(direction.getOpposite().toYRot());
                worldIn.addFreshEntity(newSitEntity);
                minoTable.setChanged();
                maid.startRiding(newSitEntity);
            }
        }
    }

    private boolean isOccupied(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlockEntityMinoTable minoTable) {
            return minoTable.getPlayersList().size() >= 4;
        }
        return true;
    }
}
