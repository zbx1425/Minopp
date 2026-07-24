package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class PendingActionGuiShard extends BadgeGuiShard {

    private static final int BACKDROP_ALPHA = 0x66;
    public static PendingActionGuiShard INSTANCE = new PendingActionGuiShard();

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        int bg = (tintColor & 0x00FFFFFF) | (alpha << 24);

        g.fill(x, y, x + WIDTH, y + HEIGHT, bg);
        g.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, BACKDROP_ALPHA << 24);
        int textW = font.width(". . .");
        int textX = x + (BadgeGuiShard.WIDTH - textW) / 2;
        int textY = y + (BadgeGuiShard.HEIGHT - font.lineHeight) / 2;
        int textColor = 0xFFFFFF | (alpha << 24);
        g.text(font, ". . .", textX, textY, textColor, true);
    }
}
