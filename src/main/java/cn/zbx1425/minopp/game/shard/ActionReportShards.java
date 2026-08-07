package cn.zbx1425.minopp.game.shard;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.shard.ActionReportShard.Lifecycle;
import cn.zbx1425.minopp.game.shard.ActionReportShard.ShardType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Function;

public class ActionReportShards {

    public static final ShardType<PlayShard> PLAY = new ShardType<>(
            Mino.id("play"), PlayShard.CODEC, Lifecycle.STATE);
    public static final ShardType<DrawShard> DRAW = new ShardType<>(
            Mino.id("draw"), DrawShard.CODEC, Lifecycle.STATE);
    public static final ShardType<PassShard> PASS = new ShardType<>(
            Mino.id("pass"), PassShard.CODEC, Lifecycle.STATE);
    public static final ShardType<SkipShard> SKIP = new ShardType<>(
            Mino.id("skip"), SkipShard.CODEC, Lifecycle.STATE);
    public static final ShardType<ReverseShard> REVERSE = new ShardType<>(
            Mino.id("reverse"), ReverseShard.CODEC, Lifecycle.STATE);
    public static final ShardType<HandSwapShard> HAND_SWAP = new ShardType<>(
            Mino.id("hand_swap"), HandSwapShard.CODEC, Lifecycle.STATE);
    public static final ShardType<HandRotateShard> HAND_ROTATE = new ShardType<>(
            Mino.id("hand_rotate"), HandRotateShard.CODEC, Lifecycle.STATE);
    public static final ShardType<GameWonShard> GAME_WON = new ShardType<>(
            Mino.id("game_won"), GameWonShard.CODEC, Lifecycle.STATE);
    public static final ShardType<SystemShard> SYSTEM = new ShardType<>(
            Mino.id("system"), SystemShard.CODEC, Lifecycle.STATE);
    public static final ShardType<RejectionShard> REJECTION = new ShardType<>(
            Mino.id("rejection"), RejectionShard.CODEC, Lifecycle.REJECTION);
    public static final ShardType<MinoShoutShard> MINO_SHOUT = new ShardType<>(
            Mino.id("mino_shout"), MinoShoutShard.CODEC, Lifecycle.OUT_OF_BAND);
    public static final ShardType<MinoShoutPenaltyShard> MINO_SHOUT_PENALTY = new ShardType<>(
            Mino.id("mino_shout_penalty"), MinoShoutPenaltyShard.CODEC, Lifecycle.OUT_OF_BAND);
    public static final ShardType<MinoDoubtShard> MINO_DOUBT = new ShardType<>(
            Mino.id("mino_doubt"), MinoDoubtShard.CODEC, Lifecycle.OUT_OF_BAND);

    public static final Map<Identifier, ShardType<?>> REGISTRY = Map.ofEntries(
            Map.entry(PLAY.id(), PLAY),
            Map.entry(DRAW.id(), DRAW),
            Map.entry(PASS.id(), PASS),
            Map.entry(SKIP.id(), SKIP),
            Map.entry(REVERSE.id(), REVERSE),
            Map.entry(HAND_SWAP.id(), HAND_SWAP),
            Map.entry(HAND_ROTATE.id(), HAND_ROTATE),
            Map.entry(GAME_WON.id(), GAME_WON),
            Map.entry(SYSTEM.id(), SYSTEM),
            Map.entry(REJECTION.id(), REJECTION),
            Map.entry(MINO_SHOUT.id(), MINO_SHOUT),
            Map.entry(MINO_SHOUT_PENALTY.id(), MINO_SHOUT_PENALTY),
            Map.entry(MINO_DOUBT.id(), MINO_DOUBT)
    );

    @SuppressWarnings("unchecked")
    private static final Codec<MapCodec<? extends ActionReportShard>> TYPE_CODEC = Codec.STRING.xmap(
            idStr -> {
                //? if <1.21
                //Identifier id = new Identifier(idStr);
                //? if >=1.21
                Identifier id = Identifier.parse(idStr);
                ShardType<?> type = REGISTRY.get(id);
                if (type == null) throw new IllegalArgumentException("Unknown shard type: " + idStr);
                return type.codec();
            },
            mapCodec -> {
                for (var entry : REGISTRY.entrySet()) {
                    if (entry.getValue().codec() == mapCodec) {
                        return entry.getKey().toString();
                    }
                }
                throw new IllegalArgumentException("Unregistered shard codec");
            }
    );

    public static final Codec<ActionReportShard> DISPATCH_CODEC = TYPE_CODEC.dispatch(
            "shard_type",
            shard -> shard.shardType().codec(),
            //? if <1.20.5
            //mapCodec -> mapCodec.codec()
            //? if >=1.20.5
            Function.identity()
    );
}
