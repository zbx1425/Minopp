package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MinoShoutShard(
    @NotNull UUID subject
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.MINO_SHOUT;
    }

    public static final MapCodec<MinoShoutShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(MinoShoutShard::subject)
    ).apply(instance, MinoShoutShard::new));
}
