package cn.zbx1425.minopp.platform.multiver;

import net.minecraft.world.level.Level;

public class WorldShim {

    public static boolean isClientSide(Level level) {
        //? if >=26.1 {
        return level.isClientSide();
        //? } else {
        /*return level.isClientSide;
        *///? }
    }
}
