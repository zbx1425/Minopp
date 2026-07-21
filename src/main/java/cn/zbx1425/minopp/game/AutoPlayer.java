package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.Mino;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.Random;
import java.util.UUID;

public class AutoPlayer {

    public boolean noWin;
    public boolean noPlayerDraw;
    public float forgetChance;
    public byte noDelay;
    public boolean startGame;
    public SevenZeroStrategy sevenZeroStrategy;

    public enum SevenZeroStrategy {
        SMART, RANDOM, PREFER, NEVER;

        public static SevenZeroStrategy fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) return SMART;
            return values()[ordinal];
        }
    }

    public AutoPlayer() {
        this(false, false, 0.2f, (byte) 0, false, SevenZeroStrategy.SMART);
    }

    public AutoPlayer(boolean noWin, boolean noPlayerDraw, float forgetChance, byte noDelay, boolean startGame, SevenZeroStrategy sevenZeroStrategy) {
        this.noWin = noWin;
        this.noPlayerDraw = noPlayerDraw;
        this.forgetChance = forgetChance;
        this.noDelay = noDelay;
        this.startGame = startGame;
        this.sevenZeroStrategy = sevenZeroStrategy;
    }

    public AutoPlayer copy() {
        return new AutoPlayer(noWin, noPlayerDraw, forgetChance, noDelay, startGame, sevenZeroStrategy);
    }

    private static final Logger LOGGER = Mino.LOGGER;

    public ActionReport playAtGame(CardGame game, CardPlayer realPlayer, MinecraftServer server, TableRuleConfig rules) {
        return playAtGame(game, realPlayer, server, rules, false);
    }

    public ActionReport playAtGame(CardGame game, CardPlayer realPlayer, MinecraftServer server, TableRuleConfig rules, boolean trace) {
        Card topCard = game.topCard;

        CardPlayer nextPlayer = game.players.get(
                (game.currentPlayerIndex + (game.isAntiClockwise ? -1 : 1) + game.players.size()) % game.players.size());
        boolean canPlayDrawCard = !noPlayerDraw || server.getPlayerList().getPlayer(nextPlayer.uuid) == null;

        if (trace) {
            LOGGER.warn("AP: Start of Decision");
            LOGGER.warn("AP: Player: {} ({}), Hand size: {}", realPlayer.name, realPlayer.uuid, realPlayer.hand.size());
            LOGGER.warn("AP: Hand: {}", realPlayer.hand);
            LOGGER.warn("AP: TopCard: {}, Phase: {}, DrawCount: {}, LastDrawnCard: {}",
                    topCard, game.currentPlayerPhase, game.drawCount, game.lastDrawnCard);
            LOGGER.warn("AP: Strategy: sevenZero={}, noWin={}, noPlayerDraw={}, canPlayDrawCard={}",
                    sevenZeroStrategy, noWin, noPlayerDraw, canPlayDrawCard);
            LOGGER.warn("AP: Rules: stacking={}, forcePlay={}, sevenRule={}, zeroRule={}, drawUntilMatch={}",
                    rules.stackingEnabled(), rules.forcePlay(), rules.sevenRuleEnabled(), rules.zeroRuleEnabled(), rules.drawUntilMatch());
        }

        // Stacking OFF + drawCount > 0: must draw
        if (!rules.stackingEnabled() && game.drawCount > 0) {
            if (trace) LOGGER.warn("AP: Decision: playNoCard (stacking OFF, drawCount > 0)");
            return game.playNoCard(realPlayer, rules);
        }

        // noWin: refuse to play last card
        if (noWin && realPlayer.hand.size() <= 1) {
            if (trace) LOGGER.warn("AP: Decision: playNoCard (noWin, hand size <= 1)");
            return game.playNoCard(realPlayer, rules);
        }

        // DISCARD_DRAWN phase: special handling for Force Play
        if (game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN) {
            if (trace) LOGGER.warn("AP: Entering DISCARD_DRAWN handler");
            return handleDiscardDrawnPhase(game, realPlayer, rules, topCard, canPlayDrawCard, trace);
        }

        // DISCARD_HAND phase: normal card selection
        if (trace) LOGGER.warn("AP: Entering DISCARD_HAND card selection");
        Card bestCard = selectBestCard(game, realPlayer, rules, topCard, canPlayDrawCard, trace);
        if (bestCard != null) {
            if (trace) LOGGER.warn("AP: Decision: play card {}", bestCard);
            return playSelectedCard(game, realPlayer, rules, bestCard, canPlayDrawCard, trace);
        }
        if (trace) LOGGER.warn("AP: Decision: playNoCard (no suitable card found)");
        return game.playNoCard(realPlayer, rules);
    }

    private ActionReport handleDiscardDrawnPhase(CardGame game, CardPlayer realPlayer, TableRuleConfig rules,
                                                  Card topCard, boolean canPlayDrawCard, boolean trace) {
        if (rules.forcePlay() && game.lastDrawnCard != null) {
            Card drawn = game.lastDrawnCard;
            if (trace) LOGGER.warn("AP: DISCARD_DRAWN: forcePlay active, lastDrawnCard={}, canPlayOn={}",
                    drawn, drawn.canPlayOn(topCard));
            if (drawn.canPlayOn(topCard)) {
                if (noWin && realPlayer.hand.size() <= 1) {
                    if (trace) LOGGER.warn("AP: Decision: playNoCard (forcePlay but noWin, hand size <= 1)");
                    return game.playNoCard(realPlayer, rules);
                }
                if (trace) LOGGER.warn("AP: Decision: play lastDrawnCard {} (forcePlay)", drawn);
                return playSelectedCard(game, realPlayer, rules, drawn, canPlayDrawCard, trace);
            } else {
                if (trace) LOGGER.warn("AP: Decision: playNoCard (forcePlay, drawn card not playable)");
                return game.playNoCard(realPlayer, rules);
            }
        }
        if (trace) LOGGER.warn("AP: DISCARD_DRAWN: no forcePlay, selecting best card");
        Card bestCard = selectBestCardForDrawnPhase(game, realPlayer, rules, topCard, canPlayDrawCard, trace);
        if (bestCard != null) {
            if (trace) LOGGER.warn("AP: Decision: play card {} (drawn phase)", bestCard);
            return playSelectedCard(game, realPlayer, rules, bestCard, canPlayDrawCard, trace);
        }
        if (trace) LOGGER.warn("AP: Decision: playNoCard (drawn phase, no suitable card)");
        return game.playNoCard(realPlayer, rules);
    }

    private Card selectBestCardForDrawnPhase(CardGame game, CardPlayer realPlayer, TableRuleConfig rules,
                                              Card topCard, boolean canPlayDrawCard, boolean trace) {
        for (Card card : realPlayer.hand) {
            if (shouldSkipSevenZero(card, rules)) {
                if (trace) LOGGER.warn("AP:   DrawnPhase skip 7/0: {}", card);
                continue;
            }
            if (!card.canPlayOn(topCard)) continue;
            if (!canPlayDrawCard && card.family == Card.Family.DRAW) {
                if (trace) LOGGER.warn("AP:   DrawnPhase skip draw card (noPlayerDraw): {}", card);
                continue;
            }
            if (noWin && realPlayer.hand.size() <= 1) {
                if (trace) LOGGER.warn("AP:   DrawnPhase skip (noWin, last card): {}", card);
                continue;
            }
            if (trace) LOGGER.warn("AP:   DrawnPhase selected: {}", card);
            return card;
        }
        return null;
    }

    private ActionReport playSelectedCard(CardGame game, CardPlayer realPlayer, TableRuleConfig rules,
                                           Card card, boolean canPlayDrawCard, boolean trace) {
        boolean shout = shouldShoutMino(realPlayer, card, game, rules);
        UUID swapTarget = null;

        if (card.suit == Card.Suit.WILD) {
            Card.Suit wildSuit = getMostCommonSuit(realPlayer);
            if (trace) LOGGER.warn("AP:   Playing wild card {}, chosen suit={}, shout={}", card, wildSuit, shout);
            return game.playCard(realPlayer, card, wildSuit, shout, rules, null);
        }

        if (rules.sevenRuleEnabled() && card.family == Card.Family.NUMBER && card.number == 7) {
            swapTarget = chooseSwapTargetSmart(game, realPlayer);
            if (trace) LOGGER.warn("AP:   Playing 7, swapTarget={}", swapTarget);
        }
        if (trace) LOGGER.warn("AP:   Playing card {}, shout={}, swapTarget={}", card, shout, swapTarget);
        return game.playCard(realPlayer, card, null, shout, rules, swapTarget);
    }

    private Card selectBestCard(CardGame game, CardPlayer realPlayer, TableRuleConfig rules,
                                 Card topCard, boolean canPlayDrawCard, boolean trace) {
        // PREFER: try 7/0 first
        if (sevenZeroStrategy == SevenZeroStrategy.PREFER) {
            Card sevenZero = findPlayableSevenZero(game, realPlayer, topCard, canPlayDrawCard);
            if (sevenZero != null) {
                if (trace) LOGGER.warn("AP:   selectBestCard: PREFER found 7/0: {}", sevenZero);
                return sevenZero;
            }
        }

        // 1. Same number, different suit (switch color - more interesting play)
        for (Card card : realPlayer.hand) {
            if (shouldSkipSevenZero(card, rules)) continue;
            if (!card.canPlayOn(topCard)) continue;
            if (card.suit == Card.Suit.WILD) continue;
            if (card.family == Card.Family.DRAW && !canPlayDrawCard) continue;
            if (card.number == topCard.number && card.suit != topCard.getEquivSuit()) {
                if (trace) LOGGER.warn("AP:   selectBestCard: same number diff suit: {}", card);
                return card;
            }
        }

        // SMART: try beneficial 7/0 here (between same-number and same-suit)
        if (sevenZeroStrategy == SevenZeroStrategy.SMART) {
            Card smartCard = findSmartSevenZero(game, realPlayer, topCard);
            if (smartCard != null) {
                if (trace) LOGGER.warn("AP:   selectBestCard: SMART beneficial 7/0: {}", smartCard);
                return smartCard;
            }
        }

        // 2. Same suit
        for (Card card : realPlayer.hand) {
            if (shouldSkipSevenZero(card, rules)) continue;
            if (!card.canPlayOn(topCard)) continue;
            if (card.suit == Card.Suit.WILD) continue;
            if (card.family == Card.Family.DRAW && !canPlayDrawCard) continue;
            if (card.suit == topCard.getEquivSuit()) {
                if (trace) LOGGER.warn("AP:   selectBestCard: same suit: {}", card);
                return card;
            }
        }

        // 3. Wild non-draw
        for (Card card : realPlayer.hand) {
            if (card.suit == Card.Suit.WILD && card.family != Card.Family.DRAW && card.canPlayOn(topCard)) {
                if (trace) LOGGER.warn("AP:   selectBestCard: wild non-draw: {}", card);
                return card;
            }
        }

        // 4. Other playable (non-wild)
        for (Card card : realPlayer.hand) {
            if (shouldSkipSevenZero(card, rules)) continue;
            if (!card.canPlayOn(topCard)) continue;
            if (card.suit == Card.Suit.WILD) continue;
            if (card.family == Card.Family.DRAW && !canPlayDrawCard) continue;
            if (trace) LOGGER.warn("AP:   selectBestCard: other playable: {}", card);
            return card;
        }

        // 5. Wild draw (last resort among normal cards)
        if (canPlayDrawCard) {
            for (Card card : realPlayer.hand) {
                if (card.suit == Card.Suit.WILD && card.family == Card.Family.DRAW && card.canPlayOn(topCard)) {
                    if (trace) LOGGER.warn("AP:   selectBestCard: wild draw: {}", card);
                    return card;
                }
            }
        }

        // NEVER: don't play 7/0 in DISCARD_HAND - just go draw
        if (sevenZeroStrategy == SevenZeroStrategy.NEVER) {
            if (trace) LOGGER.warn("AP:   selectBestCard: NEVER strategy, no non-7/0 found, will draw");
            return null;
        }

        // SMART fallback: play 7/0 even if not beneficial (better than drawing)
        if (sevenZeroStrategy == SevenZeroStrategy.SMART) {
            Card sevenZero = findPlayableSevenZero(game, realPlayer, topCard, canPlayDrawCard);
            if (sevenZero != null) {
                if (trace) LOGGER.warn("AP:   selectBestCard: SMART fallback 7/0: {}", sevenZero);
                return sevenZero;
            }
        }

        if (trace) LOGGER.warn("AP:   selectBestCard: nothing found");
        return null;
    }

    private boolean shouldSkipSevenZero(Card card, TableRuleConfig rules) {
        if (card.family != Card.Family.NUMBER) return false;
        if (sevenZeroStrategy == SevenZeroStrategy.RANDOM) return false;
        if (sevenZeroStrategy == SevenZeroStrategy.PREFER) return false;
        // SMART and NEVER skip 7/0 from normal loops (handled separately)
        if (card.number == 7 && rules.sevenRuleEnabled()) return true;
        if (card.number == 0 && rules.zeroRuleEnabled()) return true;
        return false;
    }

    private Card findPlayableSevenZero(CardGame game, CardPlayer realPlayer, Card topCard, boolean canPlayDrawCard) {
        for (Card card : realPlayer.hand) {
            if (card.family != Card.Family.NUMBER) continue;
            if (!card.canPlayOn(topCard)) continue;
            if (card.number == 7 || card.number == 0) {
                return card;
            }
        }
        return null;
    }

    private Card findSmartSevenZero(CardGame game, CardPlayer realPlayer, Card topCard) {
        // 7: swap if target has fewer cards than us - 1 (we play a card then swap)
        CardPlayer target = chooseSmartSwapTarget(game, realPlayer);
        if (target != null && target.hand.size() < realPlayer.hand.size() - 1) {
            for (Card card : realPlayer.hand) {
                if (card.family == Card.Family.NUMBER && card.number == 7 && card.canPlayOn(topCard)) {
                    return card;
                }
            }
        }

        // 0: rotate if the player whose hand we'd receive has fewer cards than us - 1
        int srcIdx = (game.players.indexOf(realPlayer) + (game.isAntiClockwise ? 1 : -1) + game.players.size()) % game.players.size();
        CardPlayer srcPlayer = game.players.get(srcIdx);
        if (srcPlayer.hand.size() < realPlayer.hand.size() - 1) {
            for (Card card : realPlayer.hand) {
                if (card.family == Card.Family.NUMBER && card.number == 0 && card.canPlayOn(topCard)) {
                    return card;
                }
            }
        }
        return null;
    }

    private boolean shouldShoutMino(CardPlayer realPlayer, Card cardToPlay, CardGame game, TableRuleConfig rules) {
        if (new Random().nextFloat() < forgetChance) return false;
        int handAfterPlay = realPlayer.hand.size() - 1;
        // Playing 7 with seven rule: after swap, our hand = target's hand size
        if (rules.sevenRuleEnabled() && cardToPlay.family == Card.Family.NUMBER && cardToPlay.number == 7) {
            CardPlayer target = chooseSmartSwapTarget(game, realPlayer);
            if (target != null) {
                handAfterPlay = target.hand.size();
            }
        }
        // Playing 0 with zero rule: after rotate, our hand = source player's hand size
        if (rules.zeroRuleEnabled() && cardToPlay.family == Card.Family.NUMBER && cardToPlay.number == 0) {
            int srcIdx = (game.players.indexOf(realPlayer) + (game.isAntiClockwise ? 1 : -1) + game.players.size()) % game.players.size();
            handAfterPlay = game.players.get(srcIdx).hand.size();
        }
        return handAfterPlay <= 1;
    }

    private UUID chooseSwapTargetSmart(CardGame game, CardPlayer realPlayer) {
        CardPlayer target = chooseSmartSwapTarget(game, realPlayer);
        return target != null ? target.uuid : null;
    }

    private CardPlayer chooseSmartSwapTarget(CardGame game, CardPlayer realPlayer) {
        CardPlayer best = null;
        int minCards = Integer.MAX_VALUE;
        for (CardPlayer p : game.players) {
            if (p.equals(realPlayer)) continue;
            if (p.hand.size() < minCards) {
                minCards = p.hand.size();
                best = p;
            }
        }
        return best;
    }

    private Card.Suit getMostCommonSuit(CardPlayer realPlayer) {
        int[] suitCount = new int[4];
        for (Card handCard : realPlayer.hand) {
            if (handCard.suit != Card.Suit.WILD) {
                suitCount[handCard.suit.ordinal()]++;
            }
        }
        Card.Suit mostCommonSuit = Card.Suit.values()[new Random().nextInt(0, 4)];
        for (int i = 0; i < 4; i++) {
            if (suitCount[i] > suitCount[mostCommonSuit.ordinal()]) {
                mostCommonSuit = Card.Suit.values()[i];
            }
        }
        return mostCommonSuit;
    }

    public static final Codec<AutoPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("NoWin", false).forGetter(a -> a.noWin),
        Codec.BOOL.optionalFieldOf("NoPlayerDraw", false).forGetter(a -> a.noPlayerDraw),
        Codec.FLOAT.optionalFieldOf("ForgetChance", 0.2f).forGetter(a -> a.forgetChance),
        Codec.BYTE.optionalFieldOf("NoDelay", (byte) 0).forGetter(a -> a.noDelay),
        Codec.BOOL.optionalFieldOf("StartGame", false).forGetter(a -> a.startGame),
        Codec.INT.optionalFieldOf("SevenZeroStrategy", 0).xmap(SevenZeroStrategy::fromOrdinal, Enum::ordinal)
            .forGetter(a -> a.sevenZeroStrategy)
    ).apply(instance, AutoPlayer::new));
}
