package cn.zbx1425.minopp.effect;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record SoundEffectEvent(int timeOffset, Optional<UUID> target, SoundEvent sound) implements EffectEvent {

    public static StreamCodec<ByteBuf, SoundEffectEvent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SoundEffectEvent::timeOffset,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), SoundEffectEvent::target,
            SoundEvent.DIRECT_STREAM_CODEC, SoundEffectEvent::sound,
            SoundEffectEvent::new
    );

    @Override
    public Type<SoundEffectEvent> type() {
        return EffectEvents.SOUND;
    }

    @Override
    public void summonClient(Level level, BlockPos origin) {
        level.playLocalSound(origin, sound, SoundSource.PLAYERS, 1, 1, false);
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin) {

    }
}