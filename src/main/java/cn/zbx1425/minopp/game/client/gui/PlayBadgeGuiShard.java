package cn.zbx1425.minopp.game.client.gui;

import cn.zbx1425.minopp.game.Card;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class PlayBadgeGuiShard extends BadgeGuiShard {

    private static final int BG_PLAY = 0xFF00A1E8;

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
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int bgColor) {
        super.render(g, font, x, y, BG_PLAY);
    }
}
