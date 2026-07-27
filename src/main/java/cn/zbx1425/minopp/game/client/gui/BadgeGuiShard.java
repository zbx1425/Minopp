package cn.zbx1425.minopp.game.client.gui;

import cn.zbx1425.minopp.platform.multiver.GuiShim;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class BadgeGuiShard extends ActionReportGuiShard {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 16;

    public Component getLabel() {
        return Component.empty();
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        int bg = (tintColor & 0x00FFFFFF) | (alpha << 24);
        g.fill(x, y, x + WIDTH, y + HEIGHT, bg);
        Component label = getLabel();
        int textW = font.width(label);
        int textX = x + (WIDTH - textW) / 2;
        int textY = y + (HEIGHT - font.lineHeight) / 2;
        int textColor = 0xFFFFFF | (alpha << 24);
        GuiShim.drawString(g, font, label, textX, textY, textColor, true);
    }

    public long getEphemeralDurationMs() {
        return 3000;
    }

    @Override
    public int getAdvance(Font font) {
        return WIDTH;
    }
}
