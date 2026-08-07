package cn.zbx1425.minopp.game.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

//? if >=1.20.5 {
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.component.FireworkExplosion;
//? } else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
*///? }

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//? if >=1.20.5 {
public record PlayerFireworkEffectEvent(int timeOffset, UUID targetPlayer, List<FireworkExplosion> firework) implements EffectEvent {

    public static PlayerFireworkEffectEvent streamDecode(FriendlyByteBuf buf) {
        int timeOffset = buf.readInt();
        UUID targetPlayer = buf.readUUID();
        int count = buf.readVarInt();
        List<FireworkExplosion> firework = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            firework.add(FireworkExplosion.STREAM_CODEC.decode(buf));
        }
        return new PlayerFireworkEffectEvent(timeOffset, targetPlayer, firework);
    }

    @Override
    public void streamEncode(FriendlyByteBuf buf) {
        buf.writeInt(timeOffset);
        buf.writeUUID(targetPlayer);
        buf.writeVarInt(firework.size());
        for (FireworkExplosion explosion : firework) {
            FireworkExplosion.STREAM_CODEC.encode(buf, explosion);
        }
    }

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
            level.createFireworks(player.getX(), player.getY() + 3, player.getZ(), 0, 0, 0, firework);
            return;
        }
        List<Entity> entities = level.getEntities((Entity) null,
                AABB.ofSize(Vec3.atLowerCornerOf(origin), 8, 8, 4),
                it -> it.getUUID().equals(targetPlayer));
        if (!entities.isEmpty()) {
            Entity entity = entities.get(0);
            level.createFireworks(entity.getX(), entity.getY() + 3, entity.getZ(), 0, 0, 0, firework);
            return;
        }
        level.createFireworks(origin.getX() + 0.5f, origin.getY() + 3, origin.getZ() + 0.5f, 0, 0, 0, firework);
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }

    public static final List<FireworkExplosion> WIN_EXPLOSION = List.of(
            new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(0xD32F2F, 0xF4511E),
                    IntList.of(0xEF9A9A, 0xFFAB91), false, false),
            new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, IntList.of(0xFDD835, 0xC0CA33),
                    IntList.of(0xFFF59D, 0xE6EE9C), false, false)
    );
}
//? } else {
/*public record PlayerFireworkEffectEvent(int timeOffset, UUID targetPlayer, CompoundTag fireworkNbt) implements EffectEvent {

    public static PlayerFireworkEffectEvent streamDecode(FriendlyByteBuf buf) {
        int timeOffset = buf.readInt();
        UUID targetPlayer = buf.readUUID();
        CompoundTag fireworkNbt = buf.readNbt();
        return new PlayerFireworkEffectEvent(timeOffset, targetPlayer, fireworkNbt);
    }

    @Override
    public void streamEncode(FriendlyByteBuf buf) {
        buf.writeInt(timeOffset);
        buf.writeUUID(targetPlayer);
        buf.writeNbt(fireworkNbt);
    }

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
            level.createFireworks(player.getX(), player.getY() + 3, player.getZ(), 0, 0, 0, fireworkNbt);
            return;
        }
        List<Entity> entities = level.getEntities((Entity) null,
                AABB.ofSize(Vec3.atLowerCornerOf(origin), 8, 8, 4),
                it -> it.getUUID().equals(targetPlayer));
        if (!entities.isEmpty()) {
            Entity entity = entities.get(0);
            level.createFireworks(entity.getX(), entity.getY() + 3, entity.getZ(), 0, 0, 0, fireworkNbt);
            return;
        }
        level.createFireworks(origin.getX() + 0.5f, origin.getY() + 3, origin.getZ() + 0.5f, 0, 0, 0, fireworkNbt);
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }

    public static CompoundTag makeWinExplosionNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag explosions = new ListTag();
        CompoundTag exp1 = new CompoundTag();
        exp1.putByte("Type", (byte) 0);
        exp1.putIntArray("Colors", new int[]{0xD32F2F, 0xF4511E});
        exp1.putIntArray("FadeColors", new int[]{0xEF9A9A, 0xFFAB91});
        explosions.add(exp1);
        CompoundTag exp2 = new CompoundTag();
        exp2.putByte("Type", (byte) 1);
        exp2.putIntArray("Colors", new int[]{0xFDD835, 0xC0CA33});
        exp2.putIntArray("FadeColors", new int[]{0xFFF59D, 0xE6EE9C});
        explosions.add(exp2);
        tag.put("Explosions", explosions);
        return tag;
    }

    public static final CompoundTag WIN_EXPLOSION = makeWinExplosionNbt();
}
*///? }
