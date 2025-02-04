package cn.zbx1425.minopp.platform.forge;

import cn.zbx1425.minopp.forge.MinoForge;
import cn.zbx1425.minopp.platform.ServerPlatform;
import jdk.jfr.Frequency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
//import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
//import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

public class CompatPacketRegistry {

//    public HashMap<ResourceLocation, CompatPacket> packets = new HashMap<>();
    public HashMap<ResourceLocation, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    public HashMap<ResourceLocation, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();

    public void registerPacket(ResourceLocation resourceLocation) {
//        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
    }

    public void registerNetworkReceiverS2C(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
//        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
        packetsS2C.put(resourceLocation, consumer);
    }

    public void registerNetworkReceiverC2S(ResourceLocation resourceLocation, ServerPlatform.C2SPacketHandler consumer) {
//        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
        packetsC2S.put(resourceLocation, consumer);
    }

    public void commit(@Nullable ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        if (player == null) {
            Consumer<FriendlyByteBuf> handlerS2C = packetsS2C.getOrDefault(id, arg -> {});
            handlerS2C.accept(payload);
        } else {
            ServerPlatform.C2SPacketHandler handlerC2S = packetsC2S.getOrDefault(id, (server, player1, arg) -> {});
            handlerC2S.handlePacket(player.getServer(), player, payload);
        }
    }

    public void sendS2C(ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        MinoForge.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new CompatPacket(id, payload));
    }

    public void sendC2S(ResourceLocation id, FriendlyByteBuf payload) {
        MinoForge.NETWORK.sendToServer(new CompatPacket(id, payload));
    }
}