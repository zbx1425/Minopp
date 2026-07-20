package cn.zbx1425.minopp.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import java.util.Random;

public class AutoPlayer {

    public boolean noWin;
    public boolean noPlayerDraw;
    public float forgetChance;
    public byte noDelay;
    public boolean startGame;

    public AutoPlayer() {
        this(false, false, 0.2f, (byte) 0, false);
    }

    public AutoPlayer(boolean noWin, boolean noPlayerDraw, float forgetChance, byte noDelay, boolean startGame) {
        this.noWin = noWin;
        this.noPlayerDraw = noPlayerDraw;
        this.forgetChance = forgetChance;
        this.noDelay = noDelay;
        this.startGame = startGame;
    }

    public ActionReport playAtGame(CardGame game, CardPlayer realPlayer, MinecraftServer server) {
        Card topCard = game.topCard;
        boolean forgetsMino = new Random().nextFloat() < forgetChance;
        boolean shoutsMino = !forgetsMino && realPlayer.hand.size() <= 2;

        // If the next player is a human player, we should not play Draw cards
        CardPlayer nextPlayer = game.players.get((game.currentPlayerIndex + (game.isAntiClockwise ? -1 : 1) + game.players.size()) % game.players.size());
        boolean canPlayDrawCard = !noPlayerDraw || server.getPlayerList().getPlayer(nextPlayer.uuid) == null;

        if (noWin) {
            if (realPlayer.hand.size() <= 1) {
                return game.playNoCard(realPlayer);
            }
        }

        // Prioritize switching suit to make things more interesting
        // If we have a card of same number but different suit
        for (Card card : realPlayer.hand) {
            if (card.number == topCard.number && card.suit != topCard.getEquivSuit() && card.suit != Card.Suit.WILD) {
                if (!canPlayDrawCard && card.family == Card.Family.DRAW) continue;
                ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                if (!result.isFail) return result;
            }
        }
        // If we have a Wild card
        for (Card card : realPlayer.hand) {
            if (card.suit == Card.Suit.WILD && card.family != Card.Family.DRAW) {
                ActionReport result = game.playCard(realPlayer, card, getMostCommonSuit(realPlayer), shoutsMino);
                if (!result.isFail) return result;
            }
        }

        // If we have a card of same suit
        for (Card card : realPlayer.hand) {
            if (card.suit == topCard.getEquivSuit() && card.suit != Card.Suit.WILD) {
                if (!canPlayDrawCard && card.family == Card.Family.DRAW) continue;
                ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                if (!result.isFail) return result;
            }
        }
        // If we have any other card
        for (Card card : realPlayer.hand) {
            if (!canPlayDrawCard && card.family == Card.Family.DRAW) continue;
            if (card.canPlayOn(topCard)) {
                if (card.suit == Card.Suit.WILD) {
                    ActionReport result = game.playCard(realPlayer, card, getMostCommonSuit(realPlayer), shoutsMino);
                    if (!result.isFail) return result;
                } else {
                    ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                    if (!result.isFail) return result;
                }
            }
        }

        // We're out of option
        return game.playNoCard(realPlayer);
    }

    private Card.Suit getMostCommonSuit(CardPlayer realPlayer) {
        int[] suitCount = new int[4];
        for (Card handCard : realPlayer.hand) {
            if (handCard.suit != Card.Suit.WILD) {
                suitCount[handCard.suit.ordinal()]++;
            }
        }
        Card.Suit mostCommonSuit = Card.Suit.values()[new Random().nextInt(0, 4)];
        for (int i = 1; i < 4; i++) {
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
        Codec.BOOL.optionalFieldOf("StartGame", false).forGetter(a -> a.startGame)
    ).apply(instance, AutoPlayer::new));
}
