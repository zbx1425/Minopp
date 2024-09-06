package cn.zbx1425.minopp;

import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.network.S2CEffectListPacket;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.effect.EffectQueue;

public class MinoClient {

    public static final EffectQueue SOUND_QUEUE = new EffectQueue();

    public static void init() {
        ClientPlatform.registerNetworkReceiver(S2CActionEphemeralPacket.ID, S2CActionEphemeralPacket.Client::handleS2C);
        ClientPlatform.registerNetworkReceiver(S2CEffectListPacket.ID, S2CEffectListPacket.Client::handleS2C);
    }
}
