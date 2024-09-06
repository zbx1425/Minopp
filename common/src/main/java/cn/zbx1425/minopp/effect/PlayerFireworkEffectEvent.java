package cn.zbx1425.minopp.effect;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PlayerFireworkEffectEvent(int timeOffset, UUID targetPlayer, List<FireworkExplosion> firework) implements EffectEvent {

    public static StreamCodec<ByteBuf, PlayerFireworkEffectEvent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlayerFireworkEffectEvent::timeOffset,
            UUIDUtil.STREAM_CODEC, PlayerFireworkEffectEvent::targetPlayer,
            FireworkExplosion.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerFireworkEffectEvent::firework,
            PlayerFireworkEffectEvent::new
    );

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public Type<PlayerFireworkEffectEvent> type() {
        return EffectEvents.PLAYER_FIREWORK;
    }

    @Override
    public void summon(Level level, BlockPos origin) {
        Player player = level.getPlayerByUUID(targetPlayer);
        if (player != null) {
            level.createFireworks(player.getX(), player.getY() + 2, player.getZ(), 0, 0, 0, firework);
        } else {
            level.createFireworks(origin.getX(), origin.getY() + 2, origin.getZ(), 0, 0, 0, firework);
        }
    }
}
