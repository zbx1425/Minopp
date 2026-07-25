package cn.zbx1425.minopp.block;

import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.shard.ActionReportShard;
import cn.zbx1425.minopp.game.shard.GameWonShard;
import cn.zbx1425.minopp.game.shard.PlayShard;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinoTableClientData {

    private final List<Pair<ActionReportShard, Long>> ephemeralShards = new ArrayList<>();
    private int lastRoundId = -1;
    @Nullable
    private UUID lastTopCardPlayer = null;
    @Nullable
    private CardGame lastGame = null;
    private List<ActionReportShard> lastStateShards = List.of();

    public void onBlockEntitySync(@Nullable CardGame currentGame, List<ActionReportShard> currentShards) {
        CardGame oldGame = this.lastGame;
        List<ActionReportShard> oldShards = this.lastStateShards;

        this.lastGame = currentGame;
        this.lastStateShards = List.copyOf(currentShards);

        if (oldGame == null && currentGame != null) {
            ephemeralShards.clear();
            lastRoundId = currentGame.roundId;
            lastTopCardPlayer = currentGame.topCardPlayer;
            return;
        }

        if (oldGame != null && currentGame == null) {
            boolean hasGameWon = currentShards.stream().anyMatch(s -> s instanceof GameWonShard);
            if (hasGameWon) {
                long now = System.currentTimeMillis();
                for (ActionReportShard old : oldShards) {
                    if (old.isNoteworthy()) {
                        ephemeralShards.add(new Pair<>(old, now));
                    }
                }
            } else {
                ephemeralShards.clear();
            }
            lastRoundId = -1;
            lastTopCardPlayer = null;
            return;
        }

        if (currentGame == null) return;
        if (currentGame.roundId == lastRoundId) return;

        long now = System.currentTimeMillis();
        boolean newStateHasPlay = currentShards.stream().anyMatch(s -> s instanceof PlayShard);

        if (newStateHasPlay && lastTopCardPlayer != null) {
            PlayShard oldPlay = findPlayShard(oldShards);
            if (oldPlay == null && oldGame != null) {
                oldPlay = new PlayShard(lastTopCardPlayer, oldGame.topCard, false);
            }
            if (oldPlay != null) {
                ephemeralShards.add(new Pair<>(oldPlay, now));
            }
        }

        for (ActionReportShard old : oldShards) {
            if (old.isNoteworthy()) {
                ephemeralShards.add(new Pair<>(old, now));
            }
        }

        ephemeralShards.removeIf(e ->
                e.getFirst().shardType().lifecycle() == ActionReportShard.Lifecycle.REJECTION);

        lastRoundId = currentGame.roundId;
        lastTopCardPlayer = currentGame.topCardPlayer;
    }

    public void addEphemeral(ActionReportShard shard) {
        ephemeralShards.add(new Pair<>(shard, System.currentTimeMillis()));
    }

    public List<Pair<ActionReportShard, Long>> getEphemeralShards() {
        return ephemeralShards;
    }

    @Nullable
    private static PlayShard findPlayShard(List<ActionReportShard> shards) {
        for (ActionReportShard s : shards) {
            if (s instanceof PlayShard ps) return ps;
        }
        return null;
    }
}
