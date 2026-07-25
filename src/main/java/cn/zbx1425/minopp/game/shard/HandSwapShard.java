package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record HandSwapShard(
    @NotNull UUID source,
    @NotNull UUID target
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.HAND_SWAP;
    }

    @Override
    public boolean isNoteworthy() {
        return true;
    }

    public static final MapCodec<HandSwapShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("source").forGetter(HandSwapShard::source),
        UUIDUtil.CODEC.fieldOf("target").forGetter(HandSwapShard::target)
    ).apply(instance, HandSwapShard::new));
}
