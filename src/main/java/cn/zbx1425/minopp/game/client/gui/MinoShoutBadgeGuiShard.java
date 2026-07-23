package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class MinoShoutBadgeGuiShard extends BadgeGuiShard {

    @Override
    public Component getLabel() {
        return Component.literal("MINO!").withStyle(ChatFormatting.GOLD);
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int tintColor, int alpha) {
        super.render(g, font, x, y, 0xFF0000, alpha);
    }
}
