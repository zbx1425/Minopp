package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

public interface ActionReportShard {

    ShardType<? extends ActionReportShard> shardType();

    default boolean isNoteworthy() {
        return false;
    }

    record ShardType<T extends ActionReportShard>(
        Identifier id,
        MapCodec<T> codec,
        Lifecycle lifecycle
    ) {}

    enum Lifecycle {
        STATE,
        REJECTION,
        OUT_OF_BAND
    }
}
