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
    public List<Card> discardDeck = new ArrayList<>();
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
        Card tobeTopCard = deck.removeLast();
        while (tobeTopCard.family() != Card.Family.NUMBER || tobeTopCard.suit() == Card.Suit.WILD) {
            deck.add(tobeTopCard);
            Collections.shuffle(deck);
            tobeTopCard = deck.removeLast();
        }
        topCard = tobeTopCard;
        return new ActionMessage(this, cardPlayer).gameStarted();
    }

    public ActionMessage playCard(CardPlayer cardPlayer, Card card, Card.Suit wildSelection) {
        ActionMessage report = new ActionMessage(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (!cardPlayer.hand.contains(card)) return report.fail(Component.translatable("game.minopp.play.not_your_card"));

        // Cut
        if (topCard.equals(card) && playerIndex != currentPlayer) {
            doDiscardCard(cardPlayer, card);
            if (cardPlayer.hand.isEmpty()) {
                return report.gameWon();
            }
            advanceTurn();
            return report.cut();
        }

        if (playerIndex != currentPlayer) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));
        if (!card.canPlayOn(topCard)) return report.fail(Component.translatable("game.minopp.play.invalid_card"));
        doDiscardCard(cardPlayer, card);
        if (cardPlayer.hand.isEmpty()) {
            return report.gameWon();
        }

        if (card.suit() == Card.Suit.WILD) {
            topCard = new Card(topCard.family(), wildSelection, topCard.number(), topCard.getActualCard());
        }
        switch (card.family()) {
            case SKIP -> isSkipping = true;
            case REVERSE -> {
                if (players.size() == 2) {
                    isSkipping = true;
                } else {
                    isAntiClockwise = !isAntiClockwise;
                }
            }
            case DRAW -> drawCount -= card.number();
        }

        advanceTurn();

        return report.played();
    }

    public ActionMessage playNoCard(CardPlayer cardPlayer) {
        ActionMessage report = new ActionMessage(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayer) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));

        boolean drawn = currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN;
        if (currentPlayerPhase == PlayerActionPhase.DISCARD_HAND) {
            // Draw card
            int drawCount = this.drawCount == 0 ? 1 : this.drawCount;
            if (!doDrawCard(cardPlayer, drawCount)) {
                return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
            }
            if (this.drawCount > 0) {
                // The draw card has already performed penalty
                // Next player doesn't have to also use draw to counteract
                this.topCard = new Card(Card.Family.NUMBER, topCard.suit(), topCard.number(), topCard.getActualCard());
                this.drawCount = 0;
            }
            currentPlayerPhase = PlayerActionPhase.DISCARD_DRAWN;
            return report.drew(drawCount);
        } else if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN) {
            advanceTurn();
        }

        return report.playedNoCard(drawn);
    }


    public ActionMessage shoutMino(CardPlayer realPlayer) {
        ActionMessage report = new ActionMessage(null, realPlayer);
        if (!realPlayer.serverHasShoutedMino) {
            if (realPlayer.hand.size() <= 2) {
                realPlayer.serverHasShoutedMino = true;
                return report.ephemeralAll(Component.translatable("game.minopp.play.mino_shout", realPlayer.name));
            } else {
                if (!doDrawCard(realPlayer, 2)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                realPlayer.serverHasShoutedMino = true; // Avoid penalty again and again
                return report.ephemeralAll(Component.translatable("game.minopp.play.mino_shout_invalid", realPlayer.name));
            }
        }
        return null;
    }

    public ActionMessage doubtMino(CardPlayer realPlayer, UUID targetPlayerWithoutHand) {
        ActionMessage report = new ActionMessage(null, realPlayer);
        CardPlayer realTargetPlayer = deAmputate(targetPlayerWithoutHand);
        if (realTargetPlayer == null) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (!players.get(currentPlayer).equals(realTargetPlayer)) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_playing"));
        } else if (realPlayer.equals(realTargetPlayer)) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_self"));
        } else if (realTargetPlayer.serverHasShoutedMino) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_shouted"));
        } else if (realPlayer.hand.size() > 1) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_hand"));
        } else {
            if (!doDrawCard(realTargetPlayer, 2)) {
                return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
            }
            return report.ephemeralAll(Component.translatable("game.minopp.play.doubt_success", realPlayer.name, realTargetPlayer.name));
        }
    }

    private void doDiscardCard(CardPlayer player, Card card) {
        discardDeck.add(topCard.getActualCard());
        topCard = card;
        player.hand.remove(card);
    }

    private boolean doDrawCard(CardPlayer cardPlayer, int drawCount) {
        if (deck.size() < drawCount) {
            Collections.shuffle(discardDeck);
            deck.addAll(discardDeck);
            discardDeck.clear();
        }
        if (deck.size() < drawCount) {
            return false;
        }
        for (int i = 0; i < drawCount; i++) {
            cardPlayer.hand.add(deck.removeLast());
        }
        return true;
    }

    private void advanceTurn() {
        CardPlayer previousPlayer = players.get(currentPlayer);

        currentPlayerPhase = PlayerActionPhase.DISCARD_HAND;
        if (isSkipping) currentPlayer = (currentPlayer + (isAntiClockwise ? -1 : 1)) % players.size();
        currentPlayer = (currentPlayer + (isAntiClockwise ? -1 : 1)) % players.size();
        if (currentPlayer < 0) currentPlayer += players.size();
        isSkipping = false;

        CardPlayer currentPlayerPlayer = players.get(currentPlayer);
        currentPlayerPlayer.serverHasShoutedMino = false;
    }

    public CardPlayer deAmputate(CardPlayer playerWithoutHand) {
        return players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
    }

    public CardPlayer deAmputate(UUID uuid) {
        return players.stream().filter(p -> p.uuid.equals(uuid)).findFirst().orElse(null);
    }

    public enum PlayerActionPhase {
        DISCARD_HAND,
        DISCARD_DRAWN,
    }

    public CardGame(CompoundTag tag) {
        currentPlayer = tag.getInt("currentPlayer");
        drawCount = tag.getInt("drawCount");
        isSkipping = tag.getBoolean("isSkipping");
        currentPlayerPhase = PlayerActionPhase.valueOf(tag.getString("currentPlayerPhase"));
        isAntiClockwise = tag.getBoolean("isAntiClockwise");
        deck = new ArrayList<>(tag.getList("deck", CompoundTag.TAG_COMPOUND).stream().map(t -> new Card((CompoundTag) t)).toList());
        discardDeck = new ArrayList<>(tag.getList("discardDeck", CompoundTag.TAG_COMPOUND).stream().map(t -> new Card((CompoundTag) t)).toList());
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
        ListTag discardDeckTag = new ListTag();
        discardDeckTag.addAll(discardDeck.stream().map(Card::toTag).toList());
        tag.put("discardDeck", discardDeckTag);
        tag.put("topCard", topCard.toTag());
        ListTag playersTag = new ListTag();
        playersTag.addAll(players.stream().map(CardPlayer::toTag).toList());
        tag.put("players", playersTag);
        return tag;
    }
}
