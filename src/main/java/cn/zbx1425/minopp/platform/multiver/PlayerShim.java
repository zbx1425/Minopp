package cn.zbx1425.minopp.platform.multiver;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class PlayerShim {

    public static void sendSystemMessage(Player player, Component message) {
        //? if >=26.1 {
        player.sendSystemMessage(message);
        //? } else {
        /*player.displayClientMessage(message, true);
         *///? }
    }

    public static UUID getGameProfileId(Player player) {
        //? if >=26.1 {
        return player.getGameProfile().id();
        //? } else {
        /*return player.getGameProfile().getId();
         *///? }
    }

    public static String getGameProfileName(Player player) {
        //? if >=26.1 {
        return player.getGameProfile().name();
        //? } else {
        /*return player.getGameProfile().getName();
         *///? }
    }
}
