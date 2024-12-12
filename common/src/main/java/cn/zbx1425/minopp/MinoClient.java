package cn.zbx1425.minopp;

import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.network.S2CEffectListPacket;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.effect.EffectQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class MinoClient {

    public static final EffectQueue SOUND_QUEUE = new EffectQueue();

    // For interfacing with platform codes
    public static double globalFovModifier = 1;
    public static boolean handCardOverlayActive = false;

    public static void init() {
        ClientPlatform.registerNetworkReceiver(S2CActionEphemeralPacket.ID, S2CActionEphemeralPacket.Client::handleS2C);
        ClientPlatform.registerNetworkReceiver(S2CEffectListPacket.ID, S2CEffectListPacket.Client::handleS2C);
    }

    public static void tick() {
        Level level = Minecraft.getInstance().level;
        if (level != null) MinoClient.SOUND_QUEUE.tick(level);
    }
}
