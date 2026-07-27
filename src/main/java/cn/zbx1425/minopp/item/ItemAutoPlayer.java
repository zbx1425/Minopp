package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
//? if <26.1 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.stats.Stats;
*///? } else {
import net.minecraft.world.item.SpawnEggItem;
//? }

//? if <26.1
//public class ItemAutoPlayer extends Item implements GroupedItem {
//? if >=26.1
public class ItemAutoPlayer extends SpawnEggItem implements GroupedItem {

    private static final ResourceKey<CreativeModeTab> SPAWN_EGGS = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
        Identifier.withDefaultNamespace("spawn_eggs"));

    public ItemAutoPlayer() {
        //? if <26.1 {
        /*super(GroupedItem.buildProperties(
            p -> p.stacksTo(1),
            Mino.id("auto_player")
        ));
        *///? } else {
        super(GroupedItem.buildProperties(
            p -> p
                .stacksTo(1)
                .spawnEgg(Mino.ENTITY_AUTO_PLAYER.get())
            ,
            Mino.id("auto_player")
        ));
        //? }
    }

//? if <26.1 {
    /*@Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack itemStack = context.getItemInHand();
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState blockState = level.getBlockState(blockPos);

        BlockPos spawnPos;
        if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
            spawnPos = blockPos;
        } else {
            spawnPos = blockPos.relative(direction);
        }

        Entity entity = Mino.ENTITY_AUTO_PLAYER.get().spawn(
            serverLevel, itemStack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG,
            true, !blockPos.equals(spawnPos) && direction == Direction.UP
        );
        if (entity != null) {
            itemStack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(itemStack);
        }
        BlockPos blockPos = hitResult.getBlockPos();
        if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, hitResult.getDirection(), itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        Entity entity = Mino.ENTITY_AUTO_PLAYER.get().spawn(
            serverLevel, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false
        );
        if (entity == null) {
            return InteractionResultHolder.pass(itemStack);
        }
        itemStack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
        return InteractionResultHolder.consume(itemStack);
    }
*///? }

    @Override
    public ResourceKey<CreativeModeTab> getTab() {
        return SPAWN_EGGS;
    }
}
