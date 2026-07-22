package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DrawShard(
    @NotNull UUID subject,
    int drawCount
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.DRAW;
    }

    public static final MapCodec<DrawShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(DrawShard::subject),
        Codec.INT.fieldOf("drawCount").forGetter(DrawShard::drawCount)
    ).apply(instance, DrawShard::new));
}
