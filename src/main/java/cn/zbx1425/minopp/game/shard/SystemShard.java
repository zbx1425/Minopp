package cn.zbx1425.minopp.game.shard;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;

public record SystemShard(
    @NotNull Component message
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.SYSTEM;
    }

    private static final Codec<Component> JSON_STRING_COMPONENT_CODEC = Codec.STRING.xmap(
        s -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(s)).getOrThrow(),
        c -> ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow().toString()
    );

    public static final MapCodec<SystemShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        JSON_STRING_COMPONENT_CODEC.fieldOf("message").forGetter(SystemShard::message)
    ).apply(instance, SystemShard::new));
}
