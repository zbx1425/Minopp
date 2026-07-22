package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class BadgeGuiShard extends ActionReportGuiShard {

    public static final int WIDTH = 48;
    public static final int HEIGHT = 16;

    public Component getLabel() {
        return Component.empty();
    }

    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int bgColor) {
        g.fill(x, y, x + WIDTH, y + HEIGHT, bgColor);
        Component label = getLabel();
        int textW = font.width(label);
        int textX = x + (WIDTH - textW) / 2;
        int textY = y + (HEIGHT - font.lineHeight) / 2;
        g.text(font, label, textX, textY, 0xFFFFFFFF, true);
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y) {
        // Not used directly; use render(g, font, x, y, bgColor) instead
    }

    @Override
    public org.joml.Vector2i getAdvance() {
        return new org.joml.Vector2i(WIDTH, HEIGHT);
    }
}
