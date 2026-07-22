package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MinoShoutPenaltyShard(
    @NotNull UUID subject,
    int drawCount
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.MINO_SHOUT_PENALTY;
    }

    public static final MapCodec<MinoShoutPenaltyShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(MinoShoutPenaltyShard::subject),
        Codec.INT.fieldOf("drawCount").forGetter(MinoShoutPenaltyShard::drawCount)
    ).apply(instance, MinoShoutPenaltyShard::new));
}
