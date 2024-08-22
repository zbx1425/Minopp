package cn.zbx1425.minopp.game;

import net.minecraft.network.chat.Component;

import java.util.*;

public class CardGame {

    public List<CardPlayer> players = new ArrayList<>();
    public int currentPlayer;

    public int drawCount;
    public boolean isSkipping;
    public PlayerActionPhase currentPlayerPhase;

    public boolean isGameActive;
    public boolean isAntiClockwise;

    public List<Card> deck = new ArrayList<>();
    public Card topCard;

    public ActionReport playCard(UUID player, Card card, Card.Suit wildSelection) {
        if (!isGameActive) return ActionReport.NO_GAME;

        CardPlayer cardPlayer = players.stream().filter(p -> p.uuid.equals(player)).findFirst().orElse(null);
        ActionReport report = new ActionReport(this, cardPlayer, card);
        int playerIndex = players.indexOf(cardPlayer);
        if (cardPlayer == null) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (!cardPlayer.hand.contains(card)) return report.fail(Component.translatable("game.minopp.play.no_card"));

        if (currentPlayerPhase == PlayerActionPhase.DRAW) {
            return report.fail(Component.translatable("game.minopp.play.must_draw"));
        }

        // Cut
        if (topCard.equals(card) && playerIndex != currentPlayer) {
            doDiscardCard(cardPlayer, card);
            advanceTurn();
            return report.cutSuccess();
        }

        if (playerIndex != currentPlayer) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));
        if (!topCard.canPlayOn(card)) return report.fail(Component.translatable("game.minopp.play.invalid_card"));
        doDiscardCard(cardPlayer, card);

        if (card.suit() == Card.Suit.WILD) {
            topCard = new Card(topCard.family(), wildSelection, topCard.number());
        }
        switch (card.family()) {
            case SKIP -> isSkipping = true;
            case REVERSE -> isAntiClockwise = !isAntiClockwise;
            case DRAW -> drawCount += card.number();
        }

        advanceTurn();

        return report.playSuccess();
    }

    public ActionReport playNoCard(UUID player) {
        if (!isGameActive) return ActionReport.NO_GAME;

        CardPlayer cardPlayer = players.stream().filter(p -> p.uuid.equals(player)).findFirst().orElse(null);
        ActionReport report = new ActionReport(this, cardPlayer, null);
        int playerIndex = players.indexOf(cardPlayer);
        if (cardPlayer == null) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayer) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));

        if (currentPlayerPhase == PlayerActionPhase.DRAW) {
            return report.fail(Component.translatable("game.minopp.play.must_draw"));
        }

        if (currentPlayerPhase == PlayerActionPhase.DISCARD_HAND) {
            currentPlayerPhase = PlayerActionPhase.DRAW;
        } else if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN) {
            advanceTurn();
        }

        return report.noCardSuccess();
    }

    public ActionReport drawCard(UUID player) {
        if (!isGameActive) return ActionReport.NO_GAME;

        CardPlayer cardPlayer = players.stream().filter(p -> p.uuid.equals(player)).findFirst().orElse(null);
        ActionReport report = new ActionReport(this, cardPlayer, null);
        int playerIndex = players.indexOf(cardPlayer);
        if (cardPlayer == null) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayer) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));
        if (currentPlayerPhase != PlayerActionPhase.DRAW) return report.fail(Component.translatable("game.minopp.play.no_draw"));

        int drawCount = this.drawCount == 0 ? 1 : this.drawCount;
        doDrawCard(cardPlayer, drawCount);
        this.drawCount = 0;
        currentPlayerPhase = PlayerActionPhase.DISCARD_DRAWN;
        return report.drawSuccess(drawCount);
    }

    private void doDiscardCard(CardPlayer player, Card card) {
        deck.add(topCard);
        topCard = card;
        player.hand.remove(card);
    }

    private void doDrawCard(CardPlayer player, int count) {
        Collections.shuffle(deck);
        for (int i = 0; i < count; i++) {
            player.hand.add(deck.removeLast());
        }
    }

    private void advanceTurn() {
        currentPlayerPhase = PlayerActionPhase.DISCARD_HAND;
        if (isSkipping) currentPlayer = (currentPlayer + (isAntiClockwise ? -1 : 1)) % players.size();
        currentPlayer = (currentPlayer + (isAntiClockwise ? -1 : 1)) % players.size();
        if (currentPlayer < 0) currentPlayer += players.size();
        isSkipping = false;
    }

    public enum PlayerActionPhase {
        DISCARD_HAND,
        DRAW,
        DISCARD_DRAWN,
    }
}
