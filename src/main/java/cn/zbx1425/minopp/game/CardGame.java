package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.effect.GrantRewardEffectEvent;
import cn.zbx1425.minopp.effect.PlayerFireworkEffectEvent;
import cn.zbx1425.minopp.effect.PlayerGlowEffectEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.*;
import java.util.function.Function;

public class CardGame {

    public ArrayList<CardPlayer> players;
    public int currentPlayerIndex;

    public int drawCount;
    public boolean isSkipping;
    public PlayerActionPhase currentPlayerPhase;

    public boolean isAntiClockwise;

    public ArrayList<Card> deck = new ArrayList<>();
    public ArrayList<Card> discardDeck = new ArrayList<>();
    public Card topCard;

    public CardGame(ArrayList<CardPlayer> players) {
        this.players = players;
    }

    private CardGame(int currentPlayerIndex, int drawCount, boolean isSkipping, PlayerActionPhase currentPlayerPhase,
                     boolean isAntiClockwise, ArrayList<Card> deck, ArrayList<Card> discardDeck, Card topCard, ArrayList<CardPlayer> players) {
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.drawCount = drawCount;
        this.isSkipping = isSkipping;
        this.currentPlayerPhase = currentPlayerPhase;
        this.isAntiClockwise = isAntiClockwise;
        this.deck = deck;
        this.discardDeck = discardDeck;
        this.topCard = topCard;
    }

    public ActionReport initiate(CardPlayer cardPlayer, int initialCardCount) {
        if (players.size() < 2) return ActionReport.NO_GAME;
        currentPlayerIndex = new Random().nextInt(players.size());
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
        while (tobeTopCard.family != Card.Family.NUMBER || tobeTopCard.suit == Card.Suit.WILD) {
            deck.add(tobeTopCard);
            Collections.shuffle(deck);
            tobeTopCard = deck.removeLast();
        }
        topCard = tobeTopCard;
        return ActionReport.builder(this, cardPlayer)
                .sound(Mino.id("game.play"), 0)
                .sound(Mino.id("game.turn_notice"), 500, players.get(currentPlayerIndex))
                .gameStarted();
    }

    public ActionReport playCard(CardPlayer cardPlayer, Card card, Card.Suit wildSelection, boolean shout) {
        ActionReport report = ActionReport.builder(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (!cardPlayer.hand.contains(card)) return report.fail(Component.translatable("game.minopp.play.not_your_card"));

        boolean isCut = false;
        // Cut
        if (topCard.equals(card) && playerIndex != currentPlayerIndex && topCard.suit != Card.Suit.WILD) {
            isCut = true;
        } else {
            if (playerIndex != currentPlayerIndex) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));
        }

        if (!card.canPlayOn(topCard)) return report.fail(Component.translatable("game.minopp.play.invalid_card"));
        if (card.suit == Card.Suit.WILD && card.family == Card.Family.DRAW) {
            for (Card otherCard : cardPlayer.hand) {
                if (otherCard.equals(card)) continue;
                if (otherCard.canPlayOn(topCard)) {
                    return report.fail(Component.translatable("game.minopp.play.rule_forbid"));
                }
            }
        }
        if (isCut) currentPlayerIndex = playerIndex;
        doDiscardCard(cardPlayer, card, report);
        if (cardPlayer.hand.isEmpty()) {
            report.sound(Mino.id("game.win"), 0);

            report.effect(new PlayerGlowEffectEvent(cardPlayer.uuid, 6 * 20));
            report.effect(new GrantRewardEffectEvent(cardPlayer.uuid));
            for (int i = 0; i < 5; i++) {
                report.effect(new PlayerFireworkEffectEvent(i * 1000 + 500, cardPlayer.uuid, PlayerFireworkEffectEvent.WIN_EXPLOSION));
            }

            return report.gameWon();
        }

        if (card.suit == Card.Suit.WILD) {
            topCard = topCard.withEquivSuit(wildSelection);
        }
        switch (card.family) {
            case SKIP -> isSkipping = true;
            case REVERSE -> {
                if (players.size() == 2) {
                    isSkipping = true;
                } else {
                    isAntiClockwise = !isAntiClockwise;
                }
            }
            case DRAW -> drawCount -= card.number;
        }
        if (shout) {
            report.combineWith(shoutMino(cardPlayer));
        }

        advanceTurn(report);

        return isCut ? report.cut() : report.played();
    }

    public ActionReport playNoCard(CardPlayer cardPlayer) {
        ActionReport report = ActionReport.builder(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayerIndex) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));

        boolean drawn = currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN;
        if (currentPlayerPhase == PlayerActionPhase.DISCARD_HAND) {
            // Draw card
            int drawCount = this.drawCount == 0 ? 1 : this.drawCount;
            if (!doDrawCard(cardPlayer, drawCount, report)) {
                return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
            }
            if (this.drawCount > 0) {
                // The draw card has already performed penalty
                // Next player doesn't have to also use draw to counteract
                this.topCard = topCard.withEquivFamily(Card.Family.NUMBER);
                this.drawCount = 0;
            }
            currentPlayerPhase = PlayerActionPhase.DISCARD_DRAWN;
            report.sound(Mino.id("game.turn_notice_again"), 500 * (drawCount > 1 ? drawCount + 1 : 1), cardPlayer);
            return report.drew(drawCount);
        } else if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN) {
            report.sound(Mino.id("game.pass"), 0);
            advanceTurn(report);
        }

        return report.playedNoCard(drawn);
    }


    public ActionReport shoutMino(CardPlayer realPlayer) {
        ActionReport report = ActionReport.builder(this, realPlayer);
        if (!realPlayer.hasShoutedMino) {
            if (realPlayer.hand.size() <= 1) {
                realPlayer.hasShoutedMino = true;
                report.sound(Mino.id("game.mino_shout"), 0);
                return report.messageAll(Component.translatable("game.minopp.play.mino_shout", realPlayer.name));
            } else {
                if (!doDrawCard(realPlayer, 2, report)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                realPlayer.hasShoutedMino = true; // Avoid penalty again and again
                report.sound(Mino.id("game.mino_shout"), 0);
                report.sound(Mino.id("game.mino_shout_invalid"), 500);
                return report.messageAll(Component.translatable("game.minopp.play.mino_shout_invalid", realPlayer.name));
            }
        }
        return null;
    }

    public ActionReport doubtMino(CardPlayer srcPlayer, UUID targetPlayerWithoutHand) {
        ActionReport report = ActionReport.builder(this, srcPlayer);
        CardPlayer targetPlayer = deAmputate(targetPlayerWithoutHand);
        if (targetPlayer == null) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (players.get(currentPlayerIndex).equals(targetPlayer)) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_playing"));
        } else if (srcPlayer.equals(targetPlayer)) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_self"));
        } else if (targetPlayer.hasShoutedMino) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_shouted"));
        } else if (targetPlayer.hand.size() > 1) {
            return report.fail(Component.translatable("game.minopp.play.doubt_target_hand"));
        } else {
            if (!doDrawCard(targetPlayer, 2, report)) {
                return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
            }
            targetPlayer.hasShoutedMino = true; // Avoid penalty again and again
            report.sound(Mino.id("game.doubt_success"), 0);
            return report.messageAll(Component.translatable("game.minopp.play.doubt_success", srcPlayer.name, targetPlayer.name));
        }
    }

    public void doDiscardCard(CardPlayer player, Card card, ActionReport report) {
        discardDeck.add(topCard.eraseEquiv());
        topCard = card;
        player.hand.remove(card);
        report.sound(Mino.id("game.play"), 0);
    }

    public boolean doDrawCard(CardPlayer cardPlayer, int drawCount, ActionReport report) {
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
            report.sound(Mino.id("game.draw"), 500 * i);
            if (drawCount > 1) {
                report.sound(Mino.id("game.draw_multi"), 500 * i + 200);
            }
        }
        return true;
    }

    private void advanceTurn(ActionReport report) {
//        CardPlayer previousPlayer = players.get(currentPlayer);

        currentPlayerPhase = PlayerActionPhase.DISCARD_HAND;
        if (isSkipping) currentPlayerIndex = (currentPlayerIndex + (isAntiClockwise ? -1 : 1)) % players.size();
        currentPlayerIndex = (currentPlayerIndex + (isAntiClockwise ? -1 : 1)) % players.size();
        if (currentPlayerIndex < 0) currentPlayerIndex += players.size();
        isSkipping = false;

        CardPlayer currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.hasShoutedMino = false;
        report.sound(Mino.id("game.turn_notice"), 500, currentPlayer);
        // report.effect(new PlayerGlowEffectEvent(0, currentPlayer.uuid, 10));
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

    public static final Codec<CardGame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("currentPlayer", 0).forGetter(g -> g.currentPlayerIndex),
        Codec.INT.optionalFieldOf("drawCount", 0).forGetter(g -> g.drawCount),
        Codec.BOOL.optionalFieldOf("isSkipping", false).forGetter(g -> g.isSkipping),
        Codec.STRING.xmap(PlayerActionPhase::valueOf, PlayerActionPhase::name)
            .optionalFieldOf("currentPlayerPhase", PlayerActionPhase.DISCARD_HAND).forGetter(g -> g.currentPlayerPhase),
        Codec.BOOL.optionalFieldOf("isAntiClockwise", false).forGetter(g -> g.isAntiClockwise),
        Card.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("deck", new ArrayList<>()).forGetter(g -> g.deck),
        Card.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("discardDeck", new ArrayList<>()).forGetter(g -> g.discardDeck),
        Card.CODEC.fieldOf("topCard").forGetter(g -> g.topCard),
        CardPlayer.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("players", new ArrayList<>()).forGetter(g -> g.players)
    ).apply(instance, CardGame::new));
}
