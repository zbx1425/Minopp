package cn.zbx1425.minopp.game.shard;

import cn.zbx1425.minopp.game.Card;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayShard(
    @NotNull UUID subject,
    @NotNull Card card,
    boolean isCut
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.PLAY;
    }

    public static final MapCodec<PlayShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(PlayShard::subject),
        Card.CODEC.fieldOf("card").forGetter(PlayShard::card),
        Codec.BOOL.fieldOf("isCut").forGetter(PlayShard::isCut)
    ).apply(instance, PlayShard::new));
}
