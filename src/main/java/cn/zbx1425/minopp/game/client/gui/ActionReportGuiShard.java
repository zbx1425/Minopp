package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class ActionReportGuiShard {

    /**
     * @param tintColor Badge: background color; Message: text color. Alpha bits are ignored (overwritten by alpha param).
     * @param alpha     0-255 opacity applied to all drawn elements.
     */
    public abstract void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha);

    /** Advance in the layout axis: x-advance (width) for Badge, y-advance (height) for Message. */
    public abstract int getAdvance(Font font);
}
