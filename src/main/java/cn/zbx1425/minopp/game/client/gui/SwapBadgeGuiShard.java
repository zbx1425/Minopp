package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.network.chat.Component;

public class SwapBadgeGuiShard extends BadgeGuiShard {

    @Override
    public Component getLabel() {
        return Component.literal("Swap");
    }
}
