package cn.zbx1425.minopp;

import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.network.S2CEnqueueSoundPacket;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.sound.SoundQueue;

public class MinoClient {

    public static final SoundQueue SOUND_QUEUE = new SoundQueue();

    public static void init() {
        ClientPlatform.registerNetworkReceiver(S2CActionEphemeralPacket.ID, S2CActionEphemeralPacket.Client::handleS2C);
        ClientPlatform.registerNetworkReceiver(S2CEnqueueSoundPacket.ID, S2CEnqueueSoundPacket.Client::handleS2C);
    }
}
