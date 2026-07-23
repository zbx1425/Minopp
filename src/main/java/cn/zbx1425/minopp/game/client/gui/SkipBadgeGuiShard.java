package cn.zbx1425.minopp.game.client.gui;

import cn.zbx1425.minopp.gui.GameOverlayLayer;
import cn.zbx1425.minopp.platform.multiver.GuiShim;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class SkipBadgeGuiShard extends BadgeGuiShard {

    @Override
    public Component getLabel() {
        return Component.literal("/");
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        int bg = (tintColor & 0x00FFFFFF) | (alpha << 24);
        g.fill(x, y, x + WIDTH, y + HEIGHT, bg);

        GuiShim.blit(g, GameOverlayLayer.ATLAS_LOCATION,  x + (WIDTH - 10) / 2, y + 3, 218, 0, 10, 10, 256, 128);
    }
}
