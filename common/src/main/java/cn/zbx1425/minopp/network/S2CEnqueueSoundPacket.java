package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.platform.ServerPlatform;
import cn.zbx1425.minopp.sound.SoundQueue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class S2CEnqueueSoundPacket {

    public static final ResourceLocation ID = Mino.id("enqueue_sound");

    public static void sendS2C(ServerPlayer target, List<SoundQueue.Event> sounds, BlockPos origin) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        SoundQueue.Event.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(packet, sounds);
        packet.writeBlockPos(origin);
        ServerPlatform.sendPacketToPlayer(target, ID, packet);
    }

    public static class Client {

        public static void handleS2C(FriendlyByteBuf packet) {
            List<SoundQueue.Event> sounds = SoundQueue.Event.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(packet);
            BlockPos origin = packet.readBlockPos();
            MinoClient.SOUND_QUEUE.addAll(sounds, origin, Minecraft.getInstance().player);
        }
    }
}
