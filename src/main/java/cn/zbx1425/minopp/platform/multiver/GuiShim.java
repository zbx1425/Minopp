package cn.zbx1425.minopp.platform.multiver;

import cn.zbx1425.minopp.Mino;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.1 {
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FontDescription;
//? }
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiShim {

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, int w, int h,
        float u, float v, int uw, int vh,
        int texW, int texH
    ) {
        //? if <26.1 {
        /*guiGraphics.blit(texture, x, y, w, h, u, v, uw, vh, texW, texH);
        *///? } else {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, uw, vh, texW, texH);
        //? }
    }

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, float u, float v,
        int w, int h,
        int texW, int texH
    ) {
        //? if <26.1 {
        /*guiGraphics.blit(texture, x, y, u, v, w, h, texW, texH);
        *///? } else {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, texW, texH);
        //? }
    }

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, int w, int h,
        float u, float v, int uw, int vh,
        int texW, int texH,
        int color
    ) {
        //? if <26.1 {
        /*float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        guiGraphics.setColor(r, g, b, a);
        guiGraphics.blit(texture, x, y, w, h, u, v, uw, vh, texW, texH);
        guiGraphics.setColor(1, 1, 1, 1);
        *///? } else {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, uw, vh, texW, texH, color);
        //? }
    }

    public static void blit(
        GuiGraphicsExtractor guiGraphics,
        Identifier texture,
        int x, int y, float u, float v,
        int w, int h,
        int texW, int texH,
        int color
    ) {
        //? if <26.1 {
        /*float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        guiGraphics.setColor(r, g, b, a);
        guiGraphics.blit(texture, x, y, u, v, w, h, texW, texH);
        guiGraphics.setColor(1, 1, 1, 1);
        *///? } else {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, texW, texH, color);
        //? }
    }

    public static void translate(GuiGraphicsExtractor guiGraphics, float x, float y) {
        //? if <26.1 {
        /*guiGraphics.pose().translate(x, y, 0);
        *///? } else {
        guiGraphics.pose().translate(x, y);
        //? }
    }

    public static void scale(GuiGraphicsExtractor guiGraphics, float x, float y) {
        //? if <26.1 {
        /*guiGraphics.pose().scale(x, y, 1);
        *///? } else {
        guiGraphics.pose().scale(x, y);
        //? }
    }

    public static void pushMatrix(GuiGraphicsExtractor guiGraphics) {
        //? if <26.1 {
        /*guiGraphics.pose().pushPose();
        *///? } else {
        guiGraphics.pose().pushMatrix();
        //? }
    }

    public static void popMatrix(GuiGraphicsExtractor guiGraphics) {
        //? if <26.1 {
        /*guiGraphics.pose().popPose();
        *///? } else {
        guiGraphics.pose().popMatrix();
        //? }
    }

    public static void drawString(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color, boolean shadow) {
        //? if <26.1
        //g.drawString(font, text, x, y, color, shadow);
        //? if >=26.1
        g.text(font, text, x, y, color, shadow);
    }

    public static void drawString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color, boolean shadow) {
        //? if <26.1
        //g.drawString(font, text, x, y, color, shadow);
        //? if >=26.1
        g.text(font, text, x, y, color, shadow);
    }

    public static void drawString(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color) {
        //? if <26.1
        //g.drawString(font, text, x, y, color);
        //? if >=26.1
        g.text(font, text, x, y, color);
    }

    public static void drawString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        //? if <26.1
        //g.drawString(font, text, x, y, color);
        //? if >=26.1
        g.text(font, text, x, y, color);
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        //? if <26.1
        //g.drawCenteredString(font, text, x, y, color);
        //? if >=26.1
        g.centeredText(font, text, x, y, color);
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color) {
        //? if <26.1
        //g.drawCenteredString(font, text, x, y, color);
        //? if >=26.1
        g.centeredText(font, text, x, y, color);
    }

//? if >=26.1 {
    public static FontDescription getMinecraftyFontDesc() {
        return new FontDescription.Resource(Identifier.withDefaultNamespace("include/default"));
    }
//? } else {
    /*public static Identifier getMinecraftyFontDesc() {
        return Mino.vanillaId("include/default");
    }
    *///? }
}
