package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SkipShard(
    @NotNull UUID skippedPlayer
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.SKIP;
    }

    public static final MapCodec<SkipShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("skippedPlayer").forGetter(SkipShard::skippedPlayer)
    ).apply(instance, SkipShard::new));
}
