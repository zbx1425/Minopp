package cn.zbx1425.minopp.game.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PlayerParticleEffectEvent(int timeOffset, UUID targetPlayer) implements EffectEvent {

    public static PlayerParticleEffectEvent streamDecode(FriendlyByteBuf buf) {
        int timeOffset = buf.readInt();
        UUID targetPlayer = buf.readUUID();
        return new PlayerParticleEffectEvent(timeOffset, targetPlayer);
    }

    @Override
    public void streamEncode(FriendlyByteBuf buf) {
        buf.writeInt(timeOffset);
        buf.writeUUID(targetPlayer);
    }

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public Type<PlayerParticleEffectEvent> type() {
        return EffectEvents.PLAYER_PARTICLE;
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {
        Entity entity = null;
        Player player = level.getPlayerByUUID(targetPlayer);
        if (player != null) {
            entity = player;
        } else {
            List<Entity> entities = level.getEntities((Entity) null,
                    AABB.ofSize(Vec3.atLowerCornerOf(origin), 8, 8, 4),
                    it -> it.getUUID().equals(targetPlayer));
            if (!entities.isEmpty()) {
                entity = entities.get(0);
            }
        }
        if (entity != null) {
            for (int i = 0; i < 5; i++) {
                //~ if <1.21 'entity.getRandom()' -> 'RandomSource.create()'
                RandomSource random = entity.getRandom();
                double xa = random.nextGaussian() * 0.02;
                double ya = random.nextGaussian() * 0.02;
                double za = random.nextGaussian() * 0.02;
                level.addParticle(ParticleTypes.ANGRY_VILLAGER,
                        entity.getRandomX(1.0), entity.getRandomY() + 1.0, entity.getRandomZ(1.0),
                        xa, ya, za);
            }
        }
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }
}
