package cn.zbx1425.minopp.game.shard;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardGame;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PassShard(
    @NotNull UUID subject,
    @NotNull CardGame.PlayerActionPhase phase
) implements ActionReportShard<PassShard> {

    @Override
    public Type type() {
        return Type.STATE;
    }

    public static final Codec<PassShard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("subject").forGetter(PassShard::subject),
        Codec.STRING.xmap(CardGame.PlayerActionPhase::valueOf, CardGame.PlayerActionPhase::name).fieldOf("phase").forGetter(PassShard::phase)
    ).apply(instance, PassShard::new));

    @Override
    public Codec<PassShard> codec() {
        return CODEC;
    }
}
