package cn.zbx1425.minopp.game.client.gui;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.client.shard.ShardResources;
import cn.zbx1425.minopp.gui.GameOverlayLayer;import cn.zbx1425.minopp.platform.multiver.GuiShim;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class PlayBadgeGuiShard extends BadgeGuiShard {

    public static final int BG_PLAY = 0xFF00A1E8;

    private final Card card;

    public PlayBadgeGuiShard(Card card) {
        this.card = card;
    }

    @Override
    public Component getLabel() {
        return Component.literal("\u25A0 ").withStyle(Style.EMPTY.withColor(card.suit.color))
                .append(card.getCardFaceName().copy().withStyle(Style.EMPTY.withColor(0xFFFFFF)));
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        int bg = (tintColor & 0x00FFFFFF) | (alpha << 24);
        int alphaWhite = (alpha << 24) | 0x00FFFFFF;
        g.fill(x, y, x + WIDTH, y + HEIGHT, bg);

        if (card.suit == Card.Suit.WILD) {
            GuiShim.blit(g, GameOverlayLayer.ATLAS_LOCATION, x + 1, y + 1, 14, 14, 228, 0, 9, 9, 256, 128, alphaWhite);
        }

        g.blitSprite(RenderPipelines.GUI_TEXTURED, ShardResources.INSTANCE.getSpriteForSuit(card.getEquivSuit()),
            x + ((card.suit == Card.Suit.WILD && card.number == -1) ? 16 : 1),
            y, 16, 16, alphaWhite);

        if (card.family == Card.Family.REVERSE) {
            GuiShim.blit(g, GameOverlayLayer.ATLAS_LOCATION,  x + 16 + 3, y + 3, 208, 0, 10, 10, 256, 128, alphaWhite);
        } else if (card.family == Card.Family.SKIP) {
            GuiShim.blit(g, GameOverlayLayer.ATLAS_LOCATION,  x + 16 + 3, y + 3, 218, 0, 10, 10, 256, 128, alphaWhite);
        } else {
            String briefNumberRepr = card.family == Card.Family.DRAW ? ("+" + -card.number) : (card.suit == Card.Suit.WILD ? "" : Integer.toString(card.number));
            int reprWidth = font.width(briefNumberRepr);
            g.text(font, briefNumberRepr, x + 16 + (16 - reprWidth) / 2, y + (16 - 8) / 2, alphaWhite);
        }
    }
}
