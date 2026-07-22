package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class DrawBadgeGuiShard extends BadgeGuiShard {

    private final int count;

    public DrawBadgeGuiShard(int count) {
        this.count = count;
    }

    @Override
    public Component getLabel() {
        int color = count > 1 ? 0xFF4444 : 0xFFFFFF;
        return Component.literal("+" + count).withStyle(Style.EMPTY.withColor(color));
    }
}
