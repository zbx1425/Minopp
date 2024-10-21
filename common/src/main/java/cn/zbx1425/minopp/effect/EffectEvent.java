package cn.zbx1425.minopp.effect;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public interface EffectEvent {

    Type<? extends EffectEvent> type();

    int timeOffset();

    Optional<UUID> target();

    void summonClient(Level level, BlockPos origin);

    // Summons the effect on the server side. TimeOffset not supported.
    void summonServer(ServerLevel level, BlockPos origin);

    record Type<T extends EffectEvent>(ResourceLocation id, StreamCodec<ByteBuf, T> streamCodec) { }
}
