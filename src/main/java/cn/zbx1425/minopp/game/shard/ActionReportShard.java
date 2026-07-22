package cn.zbx1425.minopp.game.shard;

import com.mojang.serialization.Codec;

import java.util.UUID;

public interface ActionReportShard<TShard> {

    enum Type {
        STATE,
        REJECTION,
        OUT_OF_BAND
    }

    Type type();

    Codec<TShard> codec();
}
