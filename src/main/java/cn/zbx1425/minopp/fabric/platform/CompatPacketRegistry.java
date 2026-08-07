package cn.zbx1425.minopp.fabric.platform;
//? if fabric {


import cn.zbx1425.minopp.platform.ServerPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? if >=1.20.5
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
public class CompatPacketRegistry {

    //? if >=1.20.5
    public HashMap<Identifier, CompatPacket> packets = new HashMap<>();
    public HashMap<Identifier, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    public HashMap<Identifier, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();

    public void registerPacket(Identifier resourceLocation) {
        //? if >=1.20.5
        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
    }

    public void registerNetworkReceiverS2C(Identifier resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        //? if >=1.20.5
        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
        packetsS2C.put(resourceLocation, consumer);
    }

    public void registerNetworkReceiverC2S(Identifier resourceLocation, ServerPlatform.C2SPacketHandler consumer) {
        //? if >=1.20.5
        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
        packetsC2S.put(resourceLocation, consumer);
    }

    public void commitCommon() {
        //? if >=1.20.5 {
        for (Map.Entry<Identifier, CompatPacket> packetEntry : packets.entrySet()) {
            CompatPacket packet = packetEntry.getValue();
            //~ if >=26.1 'playC2S' -> 'serverboundPlay'
            PayloadTypeRegistry.serverboundPlay().register(packet.TYPE, packet.STREAM_CODEC);
            //~ if >=26.1 'playS2C' -> 'clientboundPlay'
            PayloadTypeRegistry.clientboundPlay().register(packet.TYPE, packet.STREAM_CODEC);
        }
        for (Map.Entry<Identifier, ServerPlatform.C2SPacketHandler> packetC2S : packetsC2S.entrySet()) {
            ServerPlatform.C2SPacketHandler handlerC2S = packetC2S.getValue();
            CompatPacket packet = packets.get(packetC2S.getKey());
            ServerPlayNetworking.registerGlobalReceiver(packet.TYPE, (payload, context) -> {
                handlerC2S.handlePacket(context.server(), context.player(), payload.buffer);
                payload.buffer.release();
            });
        }
        //? } else {
        /*for (Map.Entry<Identifier, ServerPlatform.C2SPacketHandler> packetC2S : packetsC2S.entrySet()) {
            ServerPlatform.C2SPacketHandler handlerC2S = packetC2S.getValue();
            Identifier channelId = packetC2S.getKey();
            ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, responseSender) -> {
                FriendlyByteBuf copy = new FriendlyByteBuf(buf.copy());
                server.execute(() -> {
                    handlerC2S.handlePacket(server, player, copy);
                    copy.release();
                });
            });
        }
        *///? }
    }

    public void commitClient() {
        //? if >=1.20.5 {
        for (Map.Entry<Identifier, Consumer<FriendlyByteBuf>> packetS2C : packetsS2C.entrySet()) {
            Consumer<FriendlyByteBuf> handlerS2C = packetS2C.getValue();
            CompatPacket packet = packets.get(packetS2C.getKey());
            ClientPlayNetworking.registerGlobalReceiver(packet.TYPE, (payload, context) -> {
                handlerS2C.accept(payload.buffer);
                payload.buffer.release();
            });
        }
        //? } else {
        /*for (Map.Entry<Identifier, Consumer<FriendlyByteBuf>> packetS2C : packetsS2C.entrySet()) {
            Consumer<FriendlyByteBuf> handlerS2C = packetS2C.getValue();
            Identifier channelId = packetS2C.getKey();
            ClientPlayNetworking.registerGlobalReceiver(channelId, (client, handler, buf, responseSender) -> {
                FriendlyByteBuf copy = new FriendlyByteBuf(buf.copy());
                client.execute(() -> {
                    handlerS2C.accept(copy);
                    copy.release();
                });
            });
        }
        *///? }
    }

    public void sendS2C(ServerPlayer player, Identifier id, FriendlyByteBuf payload) {
        //? if >=1.20.5 {
        CompatPacket packet = packets.get(id);
        ServerPlayNetworking.send(player, packet.new Payload(payload));
        //? } else {
        /*ServerPlayNetworking.send(player, id, payload);
        *///? }
    }

    public void sendC2S(Identifier id, FriendlyByteBuf payload) {
        //? if >=1.20.5 {
        CompatPacket packet = packets.get(id);
        ClientPlayNetworking.send(packet.new Payload(payload));
        //? } else {
        /*ClientPlayNetworking.send(id, payload);
        *///? }
    }
}

//? }
