package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record GrantRewardEffectEvent(UUID targetPlayer) implements EffectEvent {

    @Override
    public Type<? extends EffectEvent> type() {
        return EffectEvents.GRANT_REWARD;
    }

    @Override
    public int timeOffset() {
        return 0;
    }

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {

    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {
        ItemStack stack = tableEntity.award.copy();
        if (stack.isEmpty()) return;
        ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(targetPlayer);
        if (player != null) {
            boolean addSuccessful = player.getInventory().add(stack);
            if (!addSuccessful) {
                ItemEntity itemEntity = player.drop(stack, false);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(player.getUUID());
                }
            }
        }
    }

    @Override
    public void encode(EffectEvent event, FriendlyByteBuf buffer) {
        buffer.writeUUID(targetPlayer);
    }

    public static GrantRewardEffectEvent decode(FriendlyByteBuf buffer) {
        return new GrantRewardEffectEvent(buffer.readUUID());
    }
}
