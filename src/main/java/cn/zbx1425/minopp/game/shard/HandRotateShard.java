package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HandRotateShard(
    boolean isAntiClockwise
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.HAND_ROTATE;
    }

    @Override
    public boolean isNoteworthy() {
        return true;
    }

    public static final MapCodec<HandRotateShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("isAntiClockwise").forGetter(HandRotateShard::isAntiClockwise)
    ).apply(instance, HandRotateShard::new));
}
