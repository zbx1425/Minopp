package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PlayerFireworkEffectEvent(int timeOffset, UUID targetPlayer) implements EffectEvent {

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public Type<PlayerFireworkEffectEvent> type() {
        return EffectEvents.PLAYER_FIREWORK;
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {
        Player player = level.getPlayerByUUID(targetPlayer);
        if (player != null) {
            level.createFireworks(player.getX(), player.getY() + 3, player.getZ(), 0, 0, 0, generateWinExplosion());
            return;
        }
        List<Entity> entities = level.getEntities((Entity)null,
                AABB.ofSize(Vec3.atLowerCornerOf(origin), 8, 8, 4),
                it -> it.getUUID().equals(targetPlayer));
        if (!entities.isEmpty()) {
            Entity entity = entities.get(0);
            level.createFireworks(entity.getX(), entity.getY() + 3, entity.getZ(), 0, 0, 0, generateWinExplosion());
            return;
        }
        level.createFireworks(origin.getX() + 0.5f, origin.getY() + 3, origin.getZ() + 0.5f, 0, 0, 0, generateWinExplosion());
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }

    @Override
    public void encode(EffectEvent event, FriendlyByteBuf buffer) {
        buffer.writeInt(timeOffset);
        buffer.writeUUID(targetPlayer);
    }

    public static PlayerFireworkEffectEvent decode(FriendlyByteBuf buffer) {
        return new PlayerFireworkEffectEvent(buffer.readInt(), buffer.readUUID());
    }

    public static CompoundTag generateWinExplosion() {
        CompoundTag explosion1 = new CompoundTag();
        explosion1.putByte("Type", (byte) FireworkRocketItem.Shape.SMALL_BALL.getId());
        explosion1.putIntArray("Colors", IntList.of(0xD32F2F, 0xF4511E));
        explosion1.putIntArray("FadeColors", IntList.of(0xEF9A9A, 0xFFAB91));
        explosion1.putBoolean("Flicker", false);
        explosion1.putBoolean("Trail", false);

        CompoundTag explosion2 = new CompoundTag();
        explosion2.putByte("Type", (byte) FireworkRocketItem.Shape.LARGE_BALL.getId());
        explosion2.putIntArray("Colors", IntList.of(0xFDD835, 0xC0CA33));
        explosion2.putIntArray("FadeColors", IntList.of(0xFFF59D, 0xE6EE9C));
        explosion2.putBoolean("Flicker", false);
        explosion2.putBoolean("Trail", false);

        ListTag explosions = new ListTag();
        explosions.add(explosion1);
        explosions.add(explosion2);

        CompoundTag tag = new CompoundTag();
        tag.put("Explosions", explosions);
        return tag;
    }
}
