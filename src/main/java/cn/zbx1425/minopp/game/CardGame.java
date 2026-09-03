package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.effect.GrantRewardEffectEvent;
import cn.zbx1425.minopp.game.effect.PlayerFireworkEffectEvent;
import cn.zbx1425.minopp.game.effect.PlayerGlowEffectEvent;
import cn.zbx1425.minopp.game.effect.PlayerParticleEffectEvent;
import cn.zbx1425.minopp.game.shard.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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
    public UUID topCardPlayer = null;
    public Card forcePlayCard = null;
    public int roundId = 0;

    public CardGame(ArrayList<CardPlayer> players) {
        this.players = players;
    }

    private CardGame(int currentPlayerIndex, int drawCount, boolean isSkipping, PlayerActionPhase currentPlayerPhase,
                     boolean isAntiClockwise, ArrayList<Card> deck, ArrayList<Card> discardDeck, Card topCard,
                     UUID topCardPlayer, Card forcePlayCard, int roundId, ArrayList<CardPlayer> players) {
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.drawCount = drawCount;
        this.isSkipping = isSkipping;
        this.currentPlayerPhase = currentPlayerPhase;
        this.isAntiClockwise = isAntiClockwise;
        this.forcePlayCard = forcePlayCard;
        this.deck = deck;
        this.discardDeck = discardDeck;
        this.topCard = topCard;
        this.topCardPlayer = topCardPlayer;
        this.roundId = roundId;
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
                player.hand.add(deck.remove(deck.size() - 1));
            }
        }
        Card tobeTopCard = deck.remove(deck.size() - 1);
        while (tobeTopCard.family != Card.Family.NUMBER || tobeTopCard.suit == Card.Suit.WILD) {
            deck.add(tobeTopCard);
            Collections.shuffle(deck);
            tobeTopCard = deck.remove(deck.size() - 1);
        }
        topCard = tobeTopCard;
        topCardPlayer = null;
        roundId = 0;
        ActionReport report = ActionReport.builder(this, cardPlayer);
        report.sound(Mino.id("game.play"), 0);
        report.sound(Mino.id("game.turn_notice"), 500, players.get(currentPlayerIndex));
        report.shard(new SystemShard(Component.translatable("game.minopp.play.game_started", cardPlayer.name)));
        return report;
    }

    public @Nullable Component validatePlayCard(CardPlayer cardPlayer, Card card, TableRuleConfig rules) {
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return Component.translatable("game.minopp.play.no_player");
        if (!cardPlayer.hand.contains(card)) return Component.translatable("game.minopp.play.not_your_card");
        if (!rules.stackingEnabled() && drawCount > 0) return Component.translatable("game.minopp.play.must_draw");

        boolean isCut = rules.jumpInEnabled() && topCard.equals(card)
                && playerIndex != currentPlayerIndex && topCard.suit != Card.Suit.WILD;
        if (!isCut && playerIndex != currentPlayerIndex) return Component.translatable("game.minopp.play.not_your_turn");

        if (!card.canPlayOn(topCard)) return Component.translatable("game.minopp.play.invalid_card");
        if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN
                && forcePlayCard != null && !card.equals(forcePlayCard))
            return Component.translatable("game.minopp.play.force_play_only_drawn");
        if (!rules.wildDrawFourFreeUse() && card.suit == Card.Suit.WILD && card.family == Card.Family.DRAW) {
            for (Card otherCard : cardPlayer.hand) {
                if (otherCard.equals(card)) continue;
                if (otherCard.canPlayOn(topCard)) return Component.translatable("game.minopp.play.rule_forbid");
            }
        }
        return null;
    }

    public ActionReport playCard(CardPlayer cardPlayer, Card card, Card.Suit wildSelection, boolean shout,
                                  TableRuleConfig rules, UUID swapTarget) {
        ActionReport report = ActionReport.builder(this, cardPlayer);
        Component rejection = validatePlayCard(cardPlayer, card, rules);
        if (rejection != null) return report.reject(rejection);

        int playerIndex = players.indexOf(cardPlayer);
        boolean isCut = rules.jumpInEnabled() && topCard.equals(card)
                && playerIndex != currentPlayerIndex && topCard.suit != Card.Suit.WILD;
        if (isCut) currentPlayerIndex = playerIndex;
        doDiscardCard(cardPlayer, card, report);
        topCardPlayer = cardPlayer.uuid;
        roundId++;

        if (card.suit == Card.Suit.WILD) {
            topCard = topCard.withEquivSuit(wildSelection);
        }

        report.shard(new PlayShard(cardPlayer.uuid, topCard, isCut));

        if (cardPlayer.hand.isEmpty()) {
            report.sound(Mino.id("game.win"), 0);
            report.effect(new PlayerGlowEffectEvent(cardPlayer.uuid, 6 * 20));
            report.effect(new GrantRewardEffectEvent(cardPlayer.uuid));
            for (int i = 0; i < 5; i++) {
                report.effect(new PlayerFireworkEffectEvent(i * 1000 + 500, cardPlayer.uuid, PlayerFireworkEffectEvent.WIN_EXPLOSION));
            }

            Map<UUID, Integer> otherHandSizes = new LinkedHashMap<>();
            for (CardPlayer p : players) {
                if (!p.equals(cardPlayer)) otherHandSizes.put(p.uuid, p.hand.size());
            }
            report.shard(new GameWonShard(cardPlayer.uuid, otherHandSizes));
            report.shouldDestroyGame = true;
            return report;
        }

        switch (card.family) {
            case SKIP -> isSkipping = true;
            case REVERSE -> {
                if (players.size() == 2) {
                    isSkipping = true;
                } else {
                    isAntiClockwise = !isAntiClockwise;
                    report.shard(new ReverseShard(isAntiClockwise));
                }
            }
            case DRAW -> drawCount -= card.number;
        }

        if (card.family == Card.Family.NUMBER) {
            if (rules.sevenRuleEnabled() && card.number == 7) {
                if (swapTarget == null) {
                    return report.reject(Component.translatable("game.minopp.play.seven_must_swap"));
                }
                CardPlayer targetPlayer = deAmputate(swapTarget);
                if (targetPlayer != null && !targetPlayer.equals(cardPlayer)) {
                    ArrayList<Card> temp = new ArrayList<>(cardPlayer.hand);
                    cardPlayer.hand.clear();
                    cardPlayer.hand.addAll(targetPlayer.hand);
                    targetPlayer.hand.clear();
                    targetPlayer.hand.addAll(temp);
                    cardPlayer.swapGeneration = roundId;
                    targetPlayer.swapGeneration = roundId;
                    report.shard(new HandSwapShard(cardPlayer.uuid, targetPlayer.uuid));
                    report.sound(Mino.id("game.swap_hand"), 300);
                }
            } else if (rules.zeroRuleEnabled() && card.number == 0) {
                rotateHands();
                report.shard(new HandRotateShard(isAntiClockwise));
                report.sound(Mino.id("game.swap_hand"), 300);
            }
        }

        if (shout) {
            report.combineWith(shoutMino(cardPlayer));
        }

        advanceTurn(report);
        return report;
    }

    public ActionReport playNoCard(CardPlayer cardPlayer, TableRuleConfig rules) {
        ActionReport report = ActionReport.builder(this, cardPlayer);
        int playerIndex = players.indexOf(cardPlayer);
        if (playerIndex == -1) return report.reject(Component.translatable("game.minopp.play.no_player"));
        if (playerIndex != currentPlayerIndex) return report.reject(Component.translatable("game.minopp.play.not_your_turn"));

        if (currentPlayerPhase == PlayerActionPhase.DISCARD_HAND) {
            int drawCount;
            if (this.drawCount > 0) {
                drawCount = this.drawCount;
                if (!doDrawCard(cardPlayer, drawCount, report)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                this.topCard = topCard.withEquivFamily(Card.Family.NUMBER);
                this.drawCount = 0;
                forcePlayCard = null;
            } else if (rules.drawUntilMatch()) {
                drawCount = doDrawUntilMatch(cardPlayer, report, rules);
                if (drawCount == 0) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                forcePlayCard = resolveForcePlay(cardPlayer.hand.get(cardPlayer.hand.size() - 1), cardPlayer, rules);
            } else {
                drawCount = 1;
                if (!doDrawCard(cardPlayer, drawCount, report)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                forcePlayCard = resolveForcePlay(cardPlayer.hand.get(cardPlayer.hand.size() - 1), cardPlayer, rules);
            }
            currentPlayerPhase = PlayerActionPhase.DISCARD_DRAWN;
            roundId++;
            report.sound(Mino.id("game.turn_notice_again"), 500 * (drawCount > 1 ? drawCount + 1 : 1), cardPlayer);
            report.shard(new DrawShard(cardPlayer.uuid, drawCount));
            return report;
        } else if (currentPlayerPhase == PlayerActionPhase.DISCARD_DRAWN) {
            if (forcePlayCard != null) {
                return report.reject(Component.translatable("game.minopp.play.force_play"));
            }
            roundId++;
            report.sound(Mino.id("game.pass"), 0);
            report.shard(new PassShard(cardPlayer.uuid, true));
            advanceTurn(report);
        }

        return report;
    }


    public ActionReport shoutMino(CardPlayer realPlayer) {
        ActionReport report = ActionReport.builder(this, realPlayer);
        if (!realPlayer.hasShoutedMino) {
            if (realPlayer.hand.size() <= 1) {
                realPlayer.hasShoutedMino = true;
                report.sound(Mino.id("game.mino_shout"), 0);
                report.shard(new MinoShoutShard(realPlayer.uuid));
                return report;
            } else {
                if (!doDrawCard(realPlayer, 2, report)) {
                    return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
                }
                realPlayer.hasShoutedMino = true;
                report.sound(Mino.id("game.mino_shout"), 0);
                report.sound(Mino.id("game.mino_shout_invalid"), 500);
                report.shard(new MinoShoutPenaltyShard(realPlayer.uuid, 2));
                return report;
            }
        }
        return null;
    }

    public ActionReport doubtMino(CardPlayer srcPlayer, UUID targetPlayerWithoutHand) {
        ActionReport report = ActionReport.builder(this, srcPlayer);
        CardPlayer targetPlayer = deAmputate(targetPlayerWithoutHand);
        if (targetPlayer == null) return report.reject(Component.translatable("game.minopp.play.no_player"));
        if (players.get(currentPlayerIndex).equals(targetPlayer)) {
            return report.reject(Component.translatable("game.minopp.play.doubt_target_playing"));
        } else if (srcPlayer.equals(targetPlayer)) {
            return report.reject(Component.translatable("game.minopp.play.doubt_target_self"));
        } else if (targetPlayer.hasShoutedMino) {
            return report.reject(Component.translatable("game.minopp.play.doubt_target_shouted"));
        } else if (targetPlayer.hand.size() > 1) {
            return report.reject(Component.translatable("game.minopp.play.doubt_target_hand"));
        } else {
            if (!doDrawCard(targetPlayer, 2, report)) {
                return report.panic(Component.translatable("game.minopp.play.deck_depleted"));
            }
            targetPlayer.hasShoutedMino = true;
            report.sound(Mino.id("game.doubt_success"), 0);
            report.shard(new MinoDoubtShard(srcPlayer.uuid, targetPlayer.uuid, 2));
            return report;
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
            players.get(i).swapGeneration = roundId;
        }
    }

    private int doDrawUntilMatch(CardPlayer cardPlayer, ActionReport report, TableRuleConfig rules) {
        int count = 0;
        while (true) {
            if (deck.isEmpty()) {
                Collections.shuffle(discardDeck);
                deck.addAll(discardDeck);
                discardDeck.clear();
            }
            if (deck.isEmpty()) break;
            Card drawn = deck.remove(deck.size() - 1);
            cardPlayer.hand.add(drawn);
            count++;
            if (drawn.canPlayOn(topCard)) {
                if (!rules.wildDrawFourFreeUse()
                        && drawn.suit == Card.Suit.WILD && drawn.family == Card.Family.DRAW) {
                    boolean hasOtherPlayable = cardPlayer.hand.stream()
                            .anyMatch(c -> !c.equals(drawn) && c.canPlayOn(topCard));
                    if (hasOtherPlayable) continue;
                }
                break;
            }
        }
        fillDrawSfx(count, cardPlayer, report);
        return count;
    }

    private Card resolveForcePlay(Card drawn, CardPlayer player, TableRuleConfig rules) {
        if (!rules.forcePlay()) return null;
        if (!drawn.canPlayOn(topCard)) return null;
        if (!rules.wildDrawFourFreeUse() && drawn.suit == Card.Suit.WILD && drawn.family == Card.Family.DRAW) {
            for (Card c : player.hand) {
                if (c.equals(drawn)) continue;
                if (c.canPlayOn(topCard)) return null;
            }
        }
        return drawn;
    }

    private void fillDrawSfx(int drawCount, CardPlayer player, ActionReport report) {
        final int SOUND_INTERVAL = 200;
        for (int i = 0; i < drawCount; i++) {
            report.sound(Mino.id("game.draw"), SOUND_INTERVAL * i);
        }
        if (drawCount > 1) {
            for (int i = drawCount - 1; i >= 0; i -= 4) {
                report.sound(Mino.id("game.draw_multi"), SOUND_INTERVAL * i + SOUND_INTERVAL / 2);
                report.effect(new PlayerParticleEffectEvent(SOUND_INTERVAL * i + SOUND_INTERVAL / 2, player.uuid));
            }
        }
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
            cardPlayer.hand.add(deck.remove(deck.size() - 1));
        }
        fillDrawSfx(drawCount, cardPlayer, report);
        return true;
    }

    private void advanceTurn(ActionReport report) {
        currentPlayerPhase = PlayerActionPhase.DISCARD_HAND;
        forcePlayCard = null;
        if (isSkipping) {
            int skippedIdx = Math.floorMod(currentPlayerIndex + (isAntiClockwise ? -1 : 1), players.size());
            report.shard(new SkipShard(players.get(skippedIdx).uuid));
            currentPlayerIndex = Math.floorMod(currentPlayerIndex + (isAntiClockwise ? -1 : 1), players.size());
        }
        currentPlayerIndex = Math.floorMod(currentPlayerIndex + (isAntiClockwise ? -1 : 1), players.size());
        isSkipping = false;

        CardPlayer currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.hasShoutedMino = false;
        report.sound(Mino.id("game.turn_notice"), 500, currentPlayer);
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
        UUIDUtil.CODEC.optionalFieldOf("topCardPlayer").forGetter(g -> Optional.ofNullable(g.topCardPlayer)),
        Card.CODEC.optionalFieldOf("forcePlayCard").forGetter(g -> Optional.ofNullable(g.forcePlayCard)),
        Codec.INT.optionalFieldOf("roundId", 0).forGetter(g -> g.roundId),
        CardPlayer.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("players").xmap(opt -> opt.orElseGet(ArrayList::new), Optional::of).forGetter(g -> g.players)
    ).apply(instance, (ci, dc, sk, ph, ac, dk, dd, tc, tcp, ldc, ri, pl) ->
        new CardGame(ci, dc, sk, ph, ac, dk, dd, tc, tcp.orElse(null), ldc.orElse(null), ri, pl)));
}
