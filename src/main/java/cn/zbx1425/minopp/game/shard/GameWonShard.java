package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public record GameWonShard(
    @NotNull UUID winner,
    @NotNull Map<UUID, Integer> otherPlayersHandSizes
) implements ActionReportShard {

    @Override
    public ShardType<? extends ActionReportShard> shardType() {
        return ActionReportShards.GAME_WON;
    }

    public static final MapCodec<GameWonShard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("winner").forGetter(GameWonShard::winner),
        Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT)
            .fieldOf("otherPlayersHandSizes").forGetter(GameWonShard::otherPlayersHandSizes)
    ).apply(instance, GameWonShard::new));
}
