package cn.zbx1425.minopp;

import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.network.S2CEnqueueSoundPacket;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.game.EffectQueue;

public class MinoClient {

    public static final EffectQueue SOUND_QUEUE = new EffectQueue();

    public static void init() {
        ClientPlatform.registerNetworkReceiver(S2CActionEphemeralPacket.ID, S2CActionEphemeralPacket.Client::handleS2C);
        ClientPlatform.registerNetworkReceiver(S2CEnqueueSoundPacket.ID, S2CEnqueueSoundPacket.Client::handleS2C);
    }
}
