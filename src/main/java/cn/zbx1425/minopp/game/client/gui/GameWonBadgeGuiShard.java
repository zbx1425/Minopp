package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class GameWonBadgeGuiShard extends BadgeGuiShard {

    private final int handSize;

    public GameWonBadgeGuiShard(int handSize) {
        this.handSize = handSize;
    }

    @Override
    public Component getLabel() {
        if (handSize == 0) {
            return Component.literal("WIN").withStyle(Style.EMPTY.withColor(0xFFFF00));
        }
        return Component.literal(String.valueOf(handSize));
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        if (handSize == 0) {
            super.render(g, font, x, y, 0xFF0000, alpha);
        } else {
            super.render(g, font, x, y, tintColor, alpha);
        }
    }
}
