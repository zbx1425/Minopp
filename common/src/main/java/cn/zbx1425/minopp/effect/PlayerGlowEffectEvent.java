package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record PlayerGlowEffectEvent(UUID targetPlayer, int duration) implements EffectEvent {

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public Type<PlayerGlowEffectEvent> type() {
        return EffectEvents.PLAYER_GLOW;
    }

    @Override
    public int timeOffset() {
        return 0;
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {

    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {
        Player player = level.getPlayerByUUID(targetPlayer);
        if (player != null) {
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration,
                    1, false, false, false));
        } else {
            Entity entity = level.getEntity(targetPlayer);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration,
                    1, false, false, false));
            }
        }
    }

    @Override
    public void encode(EffectEvent event, FriendlyByteBuf buffer) {
        buffer.writeUUID(targetPlayer);
        buffer.writeInt(duration);
    }

    public static PlayerGlowEffectEvent decode(FriendlyByteBuf buffer) {
        return new PlayerGlowEffectEvent(buffer.readUUID(), buffer.readInt());
    }
}
