package cn.zbx1425.minopp.game.shard;

import cn.zbx1425.minopp.game.Card;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DrawShard(
    @NotNull UUID subject,
    int drawCount
) implements ActionReportShard<DrawShard>  {

    @Override
    public Type type() {
        return Type.STATE;
    }

    public static final Codec<DrawShard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(DrawShard::subject),
        Codec.INT.fieldOf("drawCount").forGetter(DrawShard::drawCount)
    ).apply(instance, DrawShard::new));

    @Override
    public Codec<DrawShard> codec() {
        return CODEC;
    }
}
