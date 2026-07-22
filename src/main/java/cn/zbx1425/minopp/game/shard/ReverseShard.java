package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ReverseShard(
    boolean isAntiClockwise
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.REVERSE;
    }

    public static final MapCodec<ReverseShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("isAntiClockwise").forGetter(ReverseShard::isAntiClockwise)
    ).apply(instance, ReverseShard::new));
}
