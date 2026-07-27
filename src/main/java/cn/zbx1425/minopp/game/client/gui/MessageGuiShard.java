package cn.zbx1425.minopp.game.client.gui;

import cn.zbx1425.minopp.platform.multiver.GuiShim;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class MessageGuiShard extends ActionReportGuiShard {

    public final Component message;
    public final int color;
    public final boolean preserveColor;

    public MessageGuiShard(Component message, int color) {
        this(message, color, false);
    }

    public MessageGuiShard(Component message, int color, boolean preserveColor) {
        this.message = message;
        this.color = color;
        this.preserveColor = preserveColor;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        int backdropAlpha = (int)(0.4f * alpha);
        g.fill(x - 2, y, x + font.width(message) + 2, y + font.lineHeight, backdropAlpha << 24);
        int textColor = (tintColor & 0x00FFFFFF) | (alpha << 24);
        GuiShim.drawString(g, font, message, x, y, textColor, true);
    }

    public long getEphemeralDurationMs() {
        return 8000;
    }

    @Override
    public int getAdvance(Font font) {
        return font.lineHeight;
    }
}
