package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PlayerFireworkEffectEvent(int timeOffset, UUID targetPlayer, List<FireworkExplosion> firework) implements EffectEvent {

    public static final Serializer<PlayerFireworkEffectEvent> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(FriendlyByteBuf buf, PlayerFireworkEffectEvent event) {
            buf.writeInt(event.timeOffset);
            buf.writeUUID(event.targetPlayer);
            buf.writeCollection(event.firework, (b, f) -> {
                b.writeEnum(f.shape());
                b.writeCollection(f.colors(), FriendlyByteBuf::writeInt);
                b.writeCollection(f.fadeColors(), FriendlyByteBuf::writeInt);
                b.writeBoolean(f.hasTrail());
                b.writeBoolean(f.hasFlicker());
            });
        }

        @Override
        public PlayerFireworkEffectEvent deserialize(FriendlyByteBuf buf) {
            int timeOffset = buf.readInt();
            UUID targetPlayer = buf.readUUID();
            List<FireworkExplosion> firework = buf.readList(b -> {
                FireworkExplosion.Shape shape = b.readEnum(FireworkExplosion.Shape.class);
                IntList colors = IntList.of(b.readList(FriendlyByteBuf::readInt).stream().mapToInt(Integer::intValue).toArray());
                IntList fadeColors = IntList.of(b.readList(FriendlyByteBuf::readInt).stream().mapToInt(Integer::intValue).toArray());
                boolean hasTrail = b.readBoolean();
                boolean hasFlicker = b.readBoolean();
                return new FireworkExplosion(shape, colors, fadeColors, hasTrail, hasFlicker);
            });
            return new PlayerFireworkEffectEvent(timeOffset, targetPlayer, firework);
        }
    };

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
        List<Entity> entities = level.getEntities((Entity)null,
                AABB.ofSize(Vec3.atLowerCornerOf(origin), 8, 8, 4),
                it -> it.getUUID().equals(targetPlayer));
        if (!entities.isEmpty()) {
            Entity entity = entities.getFirst();
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
