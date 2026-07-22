package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

public interface ActionReportShard {

    ShardType<? extends ActionReportShard> shardType();

    record ShardType<T extends ActionReportShard>(
        Identifier id,
        MapCodec<T> codec,
        Lifecycle lifecycle,
        TransitionBehavior transitionBehavior
    ) {}

    enum Lifecycle {
        STATE,
        REJECTION,
        OUT_OF_BAND
    }

    enum TransitionBehavior {
        NOTEWORTHY,
        IMMEDIATE,
        TOP_CARD_STICKY
    }
}
