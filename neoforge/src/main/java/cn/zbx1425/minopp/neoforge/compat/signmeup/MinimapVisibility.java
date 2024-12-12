package cn.zbx1425.minopp.neoforge.compat.signmeup;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import org.teacon.signmeup.api.MiniMap;

public class MinimapVisibility {

    private static Object minimapInterface;

    static {
        try {
            Class.forName("org.teacon.signmeup.api.MiniMap");
            minimapInterface = new MinimapInterface();
        } catch (ClassNotFoundException ignored) {

        }
    }

    public static void tick() {
        if (minimapInterface == null) return;
        ((MinimapInterface)minimapInterface).setVisibility(!MinoClient.handCardOverlayActive);
    }

    static class MinimapInterface {

        public boolean visible = true;

        public void setVisibility(boolean visible) {
            if (this.visible == visible) return;
            this.visible = visible;
            MiniMap.getInstance().setMiniMapVisibility(Mino.MOD_ID, visible);
        }

        public boolean getVisibility() {
            return this.visible;
        }
    }
}
