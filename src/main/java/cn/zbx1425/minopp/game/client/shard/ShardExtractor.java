package cn.zbx1425.minopp.game.client.shard;

import cn.zbx1425.minopp.game.client.gui.BadgeGuiShard;
import cn.zbx1425.minopp.game.client.gui.MessageGuiShard;
import cn.zbx1425.minopp.game.shard.ActionReportShard;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface ShardExtractor<T extends ActionReportShard> {

    Map<UUID, List<BadgeGuiShard>> extractBadges(T shard);

    List<MessageGuiShard> extractMessages(T shard, Function<UUID, String> nameResolver);
}
