package cn.zbx1425.minopp.game;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import java.util.*;

public class CardGame {

    public List<CardPlayer> players;
    public int currentPlayer;

    public int drawCount;
    public boolean isSkipping;
    public PlayerActionPhase currentPlayerPhase;

    public boolean isAntiClockwise;

    public List<Card> deck = new ArrayList<>();
    public Card topCard;

    public CardGame(List<CardPlayer> players) {
        this.players = players;
    }

    public ActionMessage initiate(CardPlayer cardPlayer, int initialCardCount) {
        if (players.size() < 2) return ActionMessage.NO_GAME;
        currentPlayer = new Random().nextInt(players.size());
        drawCount = 0;
        isSkipping = false;
        currentPlayerPhase = PlayerActionPhase.DISCARD_HAND;
        isAntiClockwise = false;
        deck = Card.createDeck();
        Collections.shuffle(deck);
        for (int i = 0; i < initialCardCount; i++) {
            for (CardPlayer player : players) {
                player.hand.add(deck.removeLast());
            }
        }
        topCard = deck.removeLast();
        return new ActionMessage(this, cardPlayer).gameStarted();
    }

    public ActionMessage playCard(CardPlayer cardPlayer, Card card, Card.Suit wildSelection) {
        ActionMessage report = new ActionMessage(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.ephemeral(Component.translatable("game.minopp.play.no_player"));
        if (!cardPlayer.hand.contains(card)) return report.ephemeral(Component.translatable("game.minopp.play.not_your_card"));

        if (currentPlayerPhase == PlayerActionPhase.DRAW) {
            if (playerIndex != currentPlayer) {
                return report.ephemeral(Component.translatable("game.minopp.play.not_your_turn"));
            } else {
                return report.ephemeral(Component.translatable("game.minopp.play.must_draw"));
            }
        }

        // Cut
        if (topCard.equals(card) && playerIndex != currentPlayer) {
            doDiscardCard(cardPlayer, card);
            if (cardPlayer.hand.isEmpty()) {
                return report.gameWon();
            }
            advanceTurn();
            return report.cut();
        }

        if (playerIndex != currentPlayer) return report.ephemeral(Component.translatable("game.minopp.play.not_your_turn"));
        if (!card.canPlayOn(topCard)) return report.ephemeral(Component.translatable("game.minopp.play.invalid_card"));
        doDiscardCard(cardPlayer, card);
        if (cardPlayer.hand.isEmpty()) {
            return report.gameWon();
        }

        if (card.suit() == Card.Suit.WILD) {
            topCard = new Card(topCard.family(), wildSelection, topCard.number(), topCard);
        }
        switch (card.family()) {
            case SKIP -> isSkipping = true;
            case REVERSE -> isAntiClockwise = !isAntiClockwise;
            case DRAW -> drawCount -= card.number();
        }

        advanceTurn();

        return report.played();
    }

    public ActionMessage playNoCard(CardPlayer cardPlayer) {
        ActionMessage report = new ActionMessage(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.ephemeral(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayer) return report.ephemeral(Component.translatable("game.minopp.play.not_your_turn"));

        if (currentPlayerPhase == PlayerActionPhase.DRAW) {
            return report.ephemeral(Component.translatable("game.minopp.play.must_draw"));
        }

        if (currentPlayerPhase == PlayerActionPhase.DISCARD_HAND) {
            currentPlayerPhase = PlayerActionPhase.DRAW;
        } else if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN) {
            advanceTurn();
        }

        return report.playedNoCard();
    }

    public ActionMessage drawCard(CardPlayer cardPlayer) {
        ActionMessage report = new ActionMessage(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.ephemeral(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayer) return report.ephemeral(Component.translatable("game.minopp.play.not_your_turn"));
        if (currentPlayerPhase != PlayerActionPhase.DRAW) return report.ephemeral(Component.translatable("game.minopp.play.no_draw"));

        int drawCount = this.drawCount == 0 ? 1 : this.drawCount;
        if (deck.size() < drawCount) {
            return report.deckDepleted();
        }
        doDrawCard(cardPlayer, drawCount);
        if (this.drawCount > 0) {
            // The draw card has already performed penalty
            // Next player doesn't have to also use draw to counteract
            this.topCard = new Card(Card.Family.NUMBER, topCard.suit(), topCard.number(), topCard);
            this.drawCount = 0;
        }
        currentPlayerPhase = PlayerActionPhase.DISCARD_DRAWN;
        return report.drew(drawCount);
    }

    private void doDiscardCard(CardPlayer player, Card card) {
        deck.add(topCard.actualCard() != null ? topCard.actualCard() : topCard);
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

    public CardPlayer deAmputate(CardPlayer playerWithoutHand) {
        return players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
    }

    public enum PlayerActionPhase {
        DISCARD_HAND,
        DRAW,
        DISCARD_DRAWN,
    }

    public CardGame(CompoundTag tag) {
        currentPlayer = tag.getInt("currentPlayer");
        drawCount = tag.getInt("drawCount");
        isSkipping = tag.getBoolean("isSkipping");
        currentPlayerPhase = PlayerActionPhase.valueOf(tag.getString("currentPlayerPhase"));
        isAntiClockwise = tag.getBoolean("isAntiClockwise");
        deck = new ArrayList<>(tag.getList("deck", CompoundTag.TAG_COMPOUND).stream().map(t -> new Card((CompoundTag) t)).toList());
        topCard = new Card(tag.getCompound("topCard"));
        players = new ArrayList<>(tag.getList("players", CompoundTag.TAG_COMPOUND).stream().map(t -> new CardPlayer((CompoundTag)t)).toList());
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("currentPlayer", currentPlayer);
        tag.putInt("drawCount", drawCount);
        tag.putBoolean("isSkipping", isSkipping);
        tag.putString("currentPlayerPhase", currentPlayerPhase.name());
        tag.putBoolean("isAntiClockwise", isAntiClockwise);
        ListTag deckTag = new ListTag();
        deckTag.addAll(deck.stream().map(Card::toTag).toList());
        tag.put("deck", deckTag);
        tag.put("topCard", topCard.toTag());
        ListTag playersTag = new ListTag();
        playersTag.addAll(players.stream().map(CardPlayer::toTag).toList());
        tag.put("players", playersTag);
        return tag;
    }
}
