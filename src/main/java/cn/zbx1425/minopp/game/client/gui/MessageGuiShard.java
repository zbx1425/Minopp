package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

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
    public void render(GuiGraphicsExtractor g, int x, int y) {
    }

    @Override
    public Vector2i getAdvance() {
        return new Vector2i(0, 10);
    }
}
