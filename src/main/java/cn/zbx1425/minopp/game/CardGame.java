package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.effect.GrantRewardEffectEvent;
import cn.zbx1425.minopp.game.effect.PlayerFireworkEffectEvent;
import cn.zbx1425.minopp.game.effect.PlayerGlowEffectEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

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
    public Card lastDrawnCard = null;

    public CardGame(ArrayList<CardPlayer> players) {
        this.players = players;
    }

    private CardGame(int currentPlayerIndex, int drawCount, boolean isSkipping, PlayerActionPhase currentPlayerPhase,
                     boolean isAntiClockwise, ArrayList<Card> deck, ArrayList<Card> discardDeck, Card topCard,
                     Card lastDrawnCard, ArrayList<CardPlayer> players) {
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.drawCount = drawCount;
        this.isSkipping = isSkipping;
        this.currentPlayerPhase = currentPlayerPhase;
        this.isAntiClockwise = isAntiClockwise;
        this.lastDrawnCard = lastDrawnCard;
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

    public ActionReport playCard(CardPlayer cardPlayer, Card card, Card.Suit wildSelection, boolean shout,
                                  TableRuleConfig rules, UUID swapTarget) {
        ActionReport report = ActionReport.builder(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (!cardPlayer.hand.contains(card)) return report.fail(Component.translatable("game.minopp.play.not_your_card"));

        // Stacking OFF: if drawCount > 0, player must draw, cannot play any card
        if (!rules.stackingEnabled() && drawCount > 0) {
            return report.fail(Component.translatable("game.minopp.play.must_draw_now"));
        }

        boolean isCut = false;
        // Cut / Jump-In
        if (rules.jumpInEnabled() && topCard.equals(card) && playerIndex != currentPlayerIndex && topCard.suit != Card.Suit.WILD) {
            isCut = true;
        } else {
            if (playerIndex != currentPlayerIndex) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));
        }

        if (!card.canPlayOn(topCard)) return report.fail(Component.translatable("game.minopp.play.invalid_card"));
        // Force Play restriction: in DISCARD_DRAWN phase, only lastDrawnCard can be played
        if (rules.forcePlay() && currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN
                && lastDrawnCard != null && !card.equals(lastDrawnCard)) {
            return report.fail(Component.translatable("game.minopp.play.force_play_only_drawn"));
        }
        // WD4 restriction: only if wildDrawFourFreeUse is OFF
        if (!rules.wildDrawFourFreeUse() && card.suit == Card.Suit.WILD && card.family == Card.Family.DRAW) {
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

        // 7-0 rules (only for NUMBER family)
        MutableComponent swapMessage = null;
        if (card.family == Card.Family.NUMBER) {
            if (rules.sevenRuleEnabled() && card.number == 7 && swapTarget != null) {
                CardPlayer targetPlayer = deAmputate(swapTarget);
                if (targetPlayer != null && !targetPlayer.equals(cardPlayer)) {
                    ArrayList<Card> temp = new ArrayList<>(cardPlayer.hand);
                    cardPlayer.hand.clear();
                    cardPlayer.hand.addAll(targetPlayer.hand);
                    targetPlayer.hand.clear();
                    targetPlayer.hand.addAll(temp);
                    cardPlayer.swapGeneration++;
                    targetPlayer.swapGeneration++;
                    swapMessage = Component.translatable("game.minopp.play.swap_hands", cardPlayer.name, targetPlayer.name)
                            .withStyle(Style.EMPTY.withColor(0xFFAA00));
                    report.sound(Mino.id("game.hand_swap"), 0);
                }
            } else if (rules.zeroRuleEnabled() && card.number == 0) {
                rotateHands();
                swapMessage = Component.translatable("game.minopp.play.rotate_hands",
                                Component.translatable("gui.minopp.play.direction." + (isAntiClockwise ? "ccw" : "cw")))
                                .withStyle(Style.EMPTY.withColor(0xFFAA00));
                report.sound(Mino.id("game.hand_swap"), 0);
            }
        }

        if (shout) {
            report.combineWith(shoutMino(cardPlayer));
        }

        advanceTurn(report);

        if (swapMessage != null) {
            return isCut ? report.cutWithExtra(swapMessage) : report.playedWithExtra(swapMessage);
        }
        return isCut ? report.cut() : report.played();
    }

    public ActionReport playNoCard(CardPlayer cardPlayer, TableRuleConfig rules) {
        ActionReport report = ActionReport.builder(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.fail(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayerIndex) return report.fail(Component.translatable("game.minopp.play.not_your_turn"));

        boolean drawn = currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN;
        if (currentPlayerPhase == PlayerActionPhase.DISCARD_HAND) {
            int drawCount;
            if (this.drawCount > 0) {
                drawCount = this.drawCount;
                if (!doDrawCard(cardPlayer, drawCount, report)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                this.topCard = topCard.withEquivFamily(Card.Family.NUMBER);
                this.drawCount = 0;
                lastDrawnCard = null;
            } else if (rules.drawUntilMatch()) {
                drawCount = doDrawUntilMatch(cardPlayer, report);
                if (drawCount == 0) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                lastDrawnCard = cardPlayer.hand.getLast();
            } else {
                drawCount = 1;
                if (!doDrawCard(cardPlayer, drawCount, report)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                lastDrawnCard = cardPlayer.hand.getLast();
            }
            currentPlayerPhase = PlayerActionPhase.DISCARD_DRAWN;
            report.sound(Mino.id("game.turn_notice_again"), 500 * (drawCount > 1 ? drawCount + 1 : 1), cardPlayer);
            return report.drew(drawCount);
        } else if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN) {
            // Force Play check: only applies to the lastDrawnCard
            if (rules.forcePlay() && lastDrawnCard != null && lastDrawnCard.canPlayOn(topCard)) {
                return report.fail(Component.translatable("game.minopp.play.force_play"));
            }
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

    private void rotateHands() {
        int n = players.size();
        List<ArrayList<Card>> hands = new ArrayList<>(n);
        for (CardPlayer p : players) {
            hands.add(new ArrayList<>(p.hand));
        }
        for (int i = 0; i < n; i++) {
            int srcIndex = isAntiClockwise ? (i + 1) % n : (i - 1 + n) % n;
            players.get(i).hand.clear();
            players.get(i).hand.addAll(hands.get(srcIndex));
            players.get(i).swapGeneration++;
        }
    }

    private int doDrawUntilMatch(CardPlayer cardPlayer, ActionReport report) {
        int count = 0;
        while (true) {
            if (deck.isEmpty()) {
                Collections.shuffle(discardDeck);
                deck.addAll(discardDeck);
                discardDeck.clear();
            }
            if (deck.isEmpty()) break;
            Card drawn = deck.removeLast();
            cardPlayer.hand.add(drawn);
            count++;
            report.sound(Mino.id("game.draw"), 500 * (count - 1));
            if (count > 1) {
                report.sound(Mino.id("game.draw_multi"), 500 * (count - 1) + 200);
            }
            if (drawn.canPlayOn(topCard)) break;
        }
        return count;
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
        lastDrawnCard = null;
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
            .optionalFieldOf("deck").xmap(opt -> opt.orElseGet(ArrayList::new), Optional::of).forGetter(g -> g.deck),
        Card.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("discardDeck").xmap(opt -> opt.orElseGet(ArrayList::new), Optional::of).forGetter(g -> g.discardDeck),
        Card.CODEC.fieldOf("topCard").forGetter(g -> g.topCard),
        Card.CODEC.optionalFieldOf("lastDrawnCard").forGetter(g -> Optional.ofNullable(g.lastDrawnCard)),
        CardPlayer.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("players").xmap(opt -> opt.orElseGet(ArrayList::new), Optional::of).forGetter(g -> g.players)
    ).apply(instance, (ci, dc, sk, ph, ac, dk, dd, tc, ldc, pl) ->
        new CardGame(ci, dc, sk, ph, ac, dk, dd, tc, ldc.orElse(null), pl)));
}
