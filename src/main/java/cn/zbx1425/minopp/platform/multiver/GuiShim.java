package cn.zbx1425.minopp.platform.multiver;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class GuiShim {

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, int w, int h,
        float u, float v, int uw, int vh,
        int texW, int texH
    ) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, uw, vh, texW, texH);
    }

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, float u, float v,
        int w, int h,
        int texW, int texH
    ) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, texW, texH);
    }

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, int w, int h,
        float u, float v, int uw, int vh,
        int texW, int texH,
        int color
    ) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, uw, vh, texW, texH, color);
    }

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, float u, float v,
        int w, int h,
        int texW, int texH,
        int color
    ) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, texW, texH, color);
    }

    public static void translate(GuiGraphicsExtractor guiGraphics, float x, float y) {
        guiGraphics.pose().translate(x, y);
    }

    public static void scale(GuiGraphicsExtractor guiGraphics, float x, float y) {
        guiGraphics.pose().scale(x, y);
    }

    public static void pushMatrix(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.pose().pushMatrix();
    }

    public static void popMatrix(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.pose().popMatrix();
    }

//? if >=26.1 {
    public static FontDescription getMiencraftyFontDesc() {
        return new FontDescription.Resource(Identifier.withDefaultNamespace("include/default"));
    }
//? } else {
    /*public static Identifier getDefaultFontDesc() {
        return new Identifier.withDefaultNamespace("include/default");
    }
*///? }
}
