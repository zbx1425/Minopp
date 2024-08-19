package cn.zbx1425.minopp.neoforge;

import cn.zbx1425.minopp.Mino;
import net.neoforged.fml.common.Mod;

@Mod(Mino.MOD_ID)
public final class MinoNeoForge {
    public MinoNeoForge() {
        // Run our common setup.
        Mino.init();
    }
}
