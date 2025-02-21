package cn.zbx1425.minopp.platform.forge;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.ServerPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Optional;

public class CompatPacketRegistry {

    private static final String PROTOCOL_VERSION = "1";
    private final SimpleChannel channel;
    private int discriminator = 0;
    
    private final HashMap<ResourceLocation, Integer> packetIds = new HashMap<>();
    private final HashMap<ResourceLocation, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    private final HashMap<ResourceLocation, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();
    private final HashMap<ResourceLocation, CompatPacket> packets = new HashMap<>();

    public CompatPacketRegistry() {
        channel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mino.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
        );
    }

    public void registerPacket(ResourceLocation resourceLocation) {
        packetIds.computeIfAbsent(resourceLocation, id -> discriminator++);
        packets.computeIfAbsent(resourceLocation, CompatPacket::new);
    }

    public void registerNetworkReceiverS2C(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        int id = packetIds.computeIfAbsent(resourceLocation, rid -> discriminator++);
        CompatPacket packet = packets.computeIfAbsent(resourceLocation, CompatPacket::new);
        packetsS2C.put(resourceLocation, consumer);
        
        channel.registerMessage(
            id,
            CompatPacket.Payload.class,
            CompatPacket.Payload::encode,
            CompatPacket.Payload::decode,
            (payload, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                context.enqueueWork(() -> consumer.accept(payload.buffer));
                context.setPacketHandled(true);
            },
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public void registerNetworkReceiverC2S(ResourceLocation resourceLocation, ServerPlatform.C2SPacketHandler consumer) {
        int id = packetIds.computeIfAbsent(resourceLocation, rid -> discriminator++);
        CompatPacket packet = packets.computeIfAbsent(resourceLocation, CompatPacket::new);
        packetsC2S.put(resourceLocation, consumer);
        
        channel.registerMessage(
            id,
            CompatPacket.Payload.class,
            CompatPacket.Payload::encode,
            CompatPacket.Payload::decode,
            (payload, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                ServerPlayer player = context.getSender();
                context.enqueueWork(() -> {
                    if (player != null && player.getServer() != null) {
                        consumer.handlePacket(player.getServer(), player, payload.buffer);
                    }
                });
                context.setPacketHandled(true);
            },
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public void sendS2C(ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        CompatPacket packet = packets.get(id);
        channel.sendTo(new CompatPacket.Payload(payload), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void sendC2S(ResourceLocation id, FriendlyByteBuf payload) {
        CompatPacket packet = packets.get(id);
        channel.sendToServer(new CompatPacket.Payload(payload));
    }
}