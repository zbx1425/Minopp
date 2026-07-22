package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PassShard(
    @NotNull UUID subject,
    boolean afterDraw
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.PASS;
    }

    public static final MapCodec<PassShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(PassShard::subject),
        Codec.BOOL.fieldOf("afterDraw").forGetter(PassShard::afterDraw)
    ).apply(instance, PassShard::new));
}
