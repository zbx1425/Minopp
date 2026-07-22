package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2i;

public abstract class ActionReportGuiShard {

    public abstract void render(GuiGraphicsExtractor g, int x, int y);

    public abstract Vector2i getAdvance();
}
