package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface EffectEvent {

    Type<? extends EffectEvent> type();

    int timeOffset();

    Optional<UUID> target();

    void summonClient(Level level, BlockPos origin, boolean selfIsPartOfSourceGame);

    // Summons the effect on the server side. TimeOffset not supported.
    void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity);

    void encode(EffectEvent event, FriendlyByteBuf buffer);

    record Type<T extends EffectEvent>(ResourceLocation id, Function<FriendlyByteBuf, EffectEvent> function) { }
}
