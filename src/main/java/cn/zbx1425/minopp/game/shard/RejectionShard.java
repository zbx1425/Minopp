package cn.zbx1425.minopp.game.shard;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
//? if >=1.20.5
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;

public record RejectionShard(
    @NotNull Component message
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.REJECTION;
    }

    //? if <1.20.5 {
    /*private static final Codec<Component> JSON_STRING_COMPONENT_CODEC = Codec.STRING.xmap(
        s -> Component.Serializer.fromJson(s),
        c -> Component.Serializer.toJson(c)
    );
    *///? } else {
    private static final Codec<Component> JSON_STRING_COMPONENT_CODEC = Codec.STRING.xmap(
        s -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(s)).getOrThrow(),
        c -> ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow().toString()
    );
    //? }

    public static final MapCodec<RejectionShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        JSON_STRING_COMPONENT_CODEC.fieldOf("message").forGetter(RejectionShard::message)
    ).apply(instance, RejectionShard::new));
}
