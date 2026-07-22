package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.game.effect.EffectEvent;
import cn.zbx1425.minopp.game.effect.SoundEffectEvent;
import cn.zbx1425.minopp.game.shard.ActionReportShard;
import cn.zbx1425.minopp.game.shard.RejectionShard;
import cn.zbx1425.minopp.game.shard.SystemShard;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActionReport {

    private CardGame game;
    private CardPlayer initiator;

    public List<ActionReportShard> shards = new ArrayList<>();
    public List<EffectEvent> effects = new ArrayList<>();

    public boolean shouldDestroyGame = false;

    private ActionReport(CardGame game, CardPlayer player) {
        this.initiator = player;
        this.game = game;
    }

    public static ActionReport builder(CardGame game, CardPlayer player) {
        return new ActionReport(game, player);
    }

    public static ActionReport builder(CardGame game) {
        return builder(game, null);
    }

    public static ActionReport builder(CardPlayer player) {
        return builder(null, player);
    }

    public static ActionReport builder() {
        return builder(null, null);
    }

    public ActionReport shard(ActionReportShard shard) {
        this.shards.add(shard);
        return this;
    }

    public boolean isFail() {
        return shards.stream().anyMatch(s ->
                s.shardType().lifecycle() == ActionReportShard.Lifecycle.REJECTION);
    }

    public ActionReport combineWith(ActionReport other) {
        if (other == null) return this;
        this.shards.addAll(other.shards);
        this.shouldDestroyGame |= other.shouldDestroyGame;
        this.effects.addAll(other.effects);
        return this;
    }

    private static final int SOUND_RANGE = 16;

    public ActionReport sound(Identifier sound, int timeOffset, CardPlayer target) {
        effects.add(new SoundEffectEvent(timeOffset, Optional.of(target.uuid), SoundEvent.createFixedRangeEvent(sound, SOUND_RANGE)));
        return this;
    }

    public ActionReport sound(Identifier sound, int timeOffset) {
        effects.add(new SoundEffectEvent(timeOffset, Optional.empty(), SoundEvent.createFixedRangeEvent(sound, SOUND_RANGE)));
        return this;
    }

    public ActionReport effect(EffectEvent event) {
        effects.add(event);
        return this;
    }

    public static final ActionReport NO_GAME = ActionReport.builder()
            .shard(new SystemShard(Component.translatable("game.minopp.play.no_game")));
}
