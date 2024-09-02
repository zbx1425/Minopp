package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.platform.DummyLookupProvider;
import cn.zbx1425.minopp.sound.SoundQueue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ActionMessage {

    private CardGame game;
    private CardPlayer initiator;

    public Type type = Type.PERSISTENT;
    public Component message;

    public List<SoundQueue.Event> serverSounds = new ArrayList<>();

    public ActionMessage(CardGame game, CardPlayer player) {
        this.initiator = player;
        this.game = game;
    }

    public ActionMessage message(Component message) {
        this.type = Type.PERSISTENT;
        this.message = message;
        return this;
    }

    public ActionMessage fail(Component message) {
        this.type = Type.EPHEMERAL_INITIATOR;
        this.message = message;
        return this;
    }

    private static final int SOUND_RANGE = 16;

    public ActionMessage sound(ResourceLocation sound, int timeOffset, CardPlayer target) {
        serverSounds.add(new SoundQueue.Event(SoundEvent.createFixedRangeEvent(sound, SOUND_RANGE), timeOffset, Optional.of(target.uuid)));
        return this;
    }

    public ActionMessage sound(ResourceLocation sound, int timeOffset) {
        serverSounds.add(new SoundQueue.Event(SoundEvent.createFixedRangeEvent(sound, SOUND_RANGE), timeOffset, Optional.empty()));
        return this;
    }

    public ActionMessage ephemeralAll(Component message) {
        this.type = Type.EPHEMERAL_ALL;
        this.message = message;
        return this;
    }

    public ActionMessage played() {
        return message(Component.translatable("game.minopp.play.played",
                initiator.name, game.topCard.getDisplayName()));
    }

    public ActionMessage cut() {
        return message(Component.translatable("game.minopp.play.cut",
                initiator.name, game.topCard.getDisplayName()));
    }

    public ActionMessage drew(int drawCount) {
        return message(Component.translatable("game.minopp.play.drew",
                initiator.name, drawCount));
    }

    public ActionMessage playedNoCard(boolean drawn) {
        if (drawn) {
            return message(Component.translatable("game.minopp.play.played_no_drawn_card", initiator.name));
        } else {
            return message(Component.translatable("game.minopp.play.played_no_card", initiator.name));
        }
    }

    public ActionMessage gameDestroyed() {
        return message(Component.translatable("game.minopp.play.game_destroyed", initiator.name));
    }

    public ActionMessage gameStarted() {
        return message(Component.translatable("game.minopp.play.game_started", initiator.name));
    }

    public ActionMessage gameWon() {
        type = Type.GAME_END;
        MutableComponent result = Component.translatable("game.minopp.play.game_won", initiator.name);
        for (CardPlayer player : game.players) {
            if (!player.equals(initiator)) {
                result = result.append("\n")
                        .append(Component.translatable("game.minopp.play.game_nearly_won", player.name, player.hand.size()));
            }
        }
        return message(result);
    }

    public ActionMessage panic(Component message) {
        type = Type.GAME_END;
        return message(message);
    }

    public static final ActionMessage NO_GAME = new ActionMessage(null, null)
            .message(Component.translatable("game.minopp.play.no_game"));

    public ActionMessage(CompoundTag tag) {
        this.type = Type.valueOf(tag.getString("type"));
        this.message = Component.Serializer.fromJson(tag.getString("message"), DummyLookupProvider.INSTANCE);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.putString("message", Component.Serializer.toJson(message, DummyLookupProvider.INSTANCE));
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionMessage that = (ActionMessage) o;
        return type == that.type && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message);
    }

    public enum Type {
        PERSISTENT,
        EPHEMERAL_INITIATOR,
        EPHEMERAL_ALL,
        GAME_END;

        public boolean isEphemeral() {
            return this == EPHEMERAL_INITIATOR || this == EPHEMERAL_ALL;
        }
    }

    public record TargetedSound(ResourceLocation sound, int timeOffset, CardPlayer target) {}
}
