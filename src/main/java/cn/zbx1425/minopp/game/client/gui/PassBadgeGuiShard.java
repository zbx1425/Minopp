package cn.zbx1425.minopp.game.client.gui;

import net.minecraft.network.chat.Component;

public class PassBadgeGuiShard extends BadgeGuiShard {

    @Override
    public Component getLabel() {
        return Component.literal("Pass");
    }
}
