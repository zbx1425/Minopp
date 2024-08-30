package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.platform.DummyLookupProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;

public class ActionMessage {

    private CardGame game;
    private CardPlayer initiator;

    public boolean isEphemeral = false;
    public boolean gameShouldFinish = false;
    public Component message;

    public ActionMessage(CardGame game, CardPlayer player) {
        this.initiator = player;
        this.gameShouldFinish = false;
        this.game = game;
    }

    public ActionMessage message(Component message) {
        this.message = message;
        return this;
    }

    public ActionMessage ephemeral(Component message) {
        this.isEphemeral = true;
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
        gameShouldFinish = true;
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
        gameShouldFinish = true;
        return message(message);
    }

    public static final ActionMessage NO_GAME = new ActionMessage(null, null)
            .message(Component.translatable("game.minopp.play.no_game"));

    public ActionMessage(CompoundTag tag) {
        this.isEphemeral = tag.getBoolean("isEphemeral");
        this.gameShouldFinish = false;
        this.message = Component.Serializer.fromJson(tag.getString("message"), DummyLookupProvider.INSTANCE);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isEphemeral", isEphemeral);
        tag.putString("message", Component.Serializer.toJson(message, DummyLookupProvider.INSTANCE));
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionMessage that = (ActionMessage) o;
        return isEphemeral == that.isEphemeral && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEphemeral, message);
    }
}
