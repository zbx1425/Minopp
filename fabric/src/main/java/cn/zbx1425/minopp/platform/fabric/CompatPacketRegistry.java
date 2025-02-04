package cn.zbx1425.minopp.platform.fabric;

import cn.zbx1425.minopp.platform.ServerPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class CompatPacketRegistry {

    public HashMap<ResourceLocation, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    public HashMap<ResourceLocation, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();

    public void registerPacket(ResourceLocation resourceLocation) {

    }

    public void registerNetworkReceiverS2C(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        packetsS2C.put(resourceLocation, consumer);
    }

    public void registerNetworkReceiverC2S(ResourceLocation resourceLocation, ServerPlatform.C2SPacketHandler consumer) {
        packetsC2S.put(resourceLocation, consumer);
    }

    public void commitCommon() {
        for (Map.Entry<ResourceLocation, ServerPlatform.C2SPacketHandler> packetC2S : packetsC2S.entrySet()) {
            ServerPlatform.C2SPacketHandler handlerC2S = packetC2S.getValue();
            ServerPlayNetworking.registerGlobalReceiver(packetC2S.getKey(), (minecraftServer, serverPlayer, serverGamePacketListener, friendlyByteBuf, packetSender) -> {
//                Log.info(LogCategory.LOG, "handlerC2S: " + packetC2S.getKey() + " payload: " + friendlyByteBuf.toString());
                handlerC2S.handlePacket(minecraftServer, serverPlayer, friendlyByteBuf);
            });
        }
    }

    public void commitClient() {
        for (Map.Entry<ResourceLocation, Consumer<FriendlyByteBuf>> packetS2C : packetsS2C.entrySet()) {
            Consumer<FriendlyByteBuf> handlerS2C = packetS2C.getValue();
            ClientPlayNetworking.registerGlobalReceiver(packetS2C.getKey(), (minecraft, clientPacketListener, friendlyByteBuf, packetSender) ->  {
                handlerS2C.accept(friendlyByteBuf);
            });
        }
    }

    public void sendS2C(ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
//        Log.info(LogCategory.LOG, "sendS2C: " + id + " payload: " + payload.toString());
        ServerPlayNetworking.send(player, id, payload);
    }

    public void sendC2S(ResourceLocation id, FriendlyByteBuf payload) {
//        Log.info(LogCategory.LOG, "sendC2S: " + id + " payload: " + payload.toString());
        ClientPlayNetworking.send(id, payload);
    }
}
