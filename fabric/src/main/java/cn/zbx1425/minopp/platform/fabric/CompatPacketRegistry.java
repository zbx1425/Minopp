package cn.zbx1425.minopp.platform.fabric;

import cn.zbx1425.minopp.platform.ServerPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.function.Consumer;

public class CompatPacketRegistry {

    private final HashMap<ResourceLocation, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    private final HashMap<ResourceLocation, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();

    public void registerPacket(ResourceLocation resourceLocation) {
        // No-op in Fabric as we don't need pre-registration
    }

    public void registerNetworkReceiverS2C(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        packetsS2C.put(resourceLocation, consumer);
    }

    public void registerNetworkReceiverC2S(ResourceLocation resourceLocation, ServerPlatform.C2SPacketHandler consumer) {
        packetsC2S.put(resourceLocation, consumer);
    }

    public void commitCommon() {
        for (var entry : packetsC2S.entrySet()) {
            ServerPlayNetworking.registerGlobalReceiver(entry.getKey(), (server, player, handler, buf, responseSender) -> {
                entry.getValue().handlePacket(server, player, buf);
            });
        }
    }

    public void commitClient() {
        for (var entry : packetsS2C.entrySet()) {
            ClientPlayNetworking.registerGlobalReceiver(entry.getKey(), (client, handler, buf, responseSender) -> {
                entry.getValue().accept(buf);
            });
        }
    }

    public void sendS2C(ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        ServerPlayNetworking.send(player, id, payload);
    }

    public void sendC2S(ResourceLocation id, FriendlyByteBuf payload) {
        ClientPlayNetworking.send(id, payload);
    }
}
