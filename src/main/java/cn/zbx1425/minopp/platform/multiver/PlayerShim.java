package cn.zbx1425.minopp.platform.multiver;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class PlayerShim {

    public static void sendSystemMessage(Player player, Component message) {
        //? if >=26.1 {
        player.sendSystemMessage(message);
        //? } else {
        /*player.displayClientMessage(message, true);
         *///? }
    }
}
