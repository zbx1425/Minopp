package cn.zbx1425.minopp.effect;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public interface EffectEvent {

    Type<? extends EffectEvent> type();

    int timeOffset();

    Optional<UUID> target();

    void summon(Level level, BlockPos origin);

    record Type<T extends EffectEvent>(ResourceLocation id, StreamCodec<ByteBuf, T> streamCodec) { }
}
