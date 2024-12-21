package cn.zbx1425.minopp;

import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.network.S2CEffectListPacket;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.effect.EffectQueue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

public class MinoClient {

    public static final EffectQueue SOUND_QUEUE = new EffectQueue();

    public static final KeyMapping KEY_SHOUT_MODIFIER = new KeyMapping("key.minopp.shout_modifier", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.minopp");

    // For interfacing with platform codes
    public static double globalFovModifier = 1;
    public static boolean handCardOverlayActive = false;

    public static void init() {
        ClientPlatform.registerKeyBinding(KEY_SHOUT_MODIFIER);
        ClientPlatform.registerNetworkReceiver(S2CActionEphemeralPacket.ID, S2CActionEphemeralPacket.Client::handleS2C);
        ClientPlatform.registerNetworkReceiver(S2CEffectListPacket.ID, S2CEffectListPacket.Client::handleS2C);
    }

    public static void tick() {
        Level level = Minecraft.getInstance().level;
        if (level != null) MinoClient.SOUND_QUEUE.tick(level);
    }
}
