package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MinoDoubtShard(
    @NotNull UUID source,
    @NotNull UUID target,
    int drawCount
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.MINO_DOUBT;
    }

    public static final MapCodec<MinoDoubtShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("source").forGetter(MinoDoubtShard::source),
        UUIDUtil.CODEC.fieldOf("target").forGetter(MinoDoubtShard::target),
        Codec.INT.fieldOf("drawCount").forGetter(MinoDoubtShard::drawCount)
    ).apply(instance, MinoDoubtShard::new));
}
