package cn.zbx1425.minopp.game;

import net.minecraft.network.chat.Component;

public class ActionReport {

    public final CardGame game;

    public CardPlayer initiator;
    public Card discardingCard;

    public boolean isSuccess;
    public Component resultMessage;

    public CardPlayer newActivePlayer;

    public ActionReport(CardGame game, CardPlayer player, Card card) {
        this.initiator = player;
        this.discardingCard = card;
        this.game = game;
    }

    public ActionReport fail(Component message) {
        this.isSuccess = false;
        this.resultMessage = message;
        this.newActivePlayer = game == null ? null : game.players.get(game.currentPlayer);
        return this;
    }

    public ActionReport playSuccess() {
        this.isSuccess = true;
        this.resultMessage = Component.translatable("game.minopp.play.success",
                initiator.name, discardingCard.getDisplayName());
        this.discardingCard = game.topCard;
        this.newActivePlayer = game.players.get(game.currentPlayer);
        return this;
    }

    public ActionReport cutSuccess() {
        this.isSuccess = true;
        this.resultMessage = Component.translatable("game.minopp.play.cut_success",
                initiator.name, discardingCard.getDisplayName());
        this.discardingCard = game.topCard;
        this.newActivePlayer = game.players.get(game.currentPlayer);
        return this;
    }

    public ActionReport drawSuccess(int drawCount) {
        this.isSuccess = true;
        this.resultMessage = Component.translatable("game.minopp.play.draw_success",
                initiator.name, drawCount);
        this.newActivePlayer = game.players.get(game.currentPlayer);
        return this;
    }

    public ActionReport noCardSuccess() {
        this.isSuccess = true;
        this.resultMessage = Component.translatable("game.minopp.play.no_op_success", initiator.name);
        this.newActivePlayer = game.players.get(game.currentPlayer);
        return this;
    }

    public static final ActionReport NO_GAME = new ActionReport(null, null, null)
            .fail(Component.translatable("game.minopp.play.no_game"));
}
