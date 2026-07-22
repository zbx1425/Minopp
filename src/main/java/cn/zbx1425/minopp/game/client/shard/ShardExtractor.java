package cn.zbx1425.minopp.game.client.shard;

import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.client.gui.ActionReportGuiShard;

import java.util.List;

public interface ShardExtractor<TShard> {

    List<ActionReportGuiShard> extract(TShard shard, CardGame game);
}
