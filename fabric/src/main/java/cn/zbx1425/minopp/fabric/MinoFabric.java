package cn.zbx1425.minopp.fabric;

import cn.zbx1425.minopp.Mino;
import net.fabricmc.api.ModInitializer;

public final class MinoFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Mino.init();
    }
}
