package cn.zbx1425.minopp.platform.forge;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.ServerPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
//import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
//import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

public class CompatPacketRegistry {

    private static final String PROTOCOL_VERSION = "1";
    private final SimpleChannel channel;

    public HashMap<ResourceLocation, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    public HashMap<ResourceLocation, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();

    public CompatPacketRegistry() {
        channel = NetworkRegistry.ChannelBuilder.named(Mino.id("network"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .serverAcceptedVersions(ignored -> true)
                .clientAcceptedVersions(ignored -> true)
                .simpleChannel();
        channel.registerMessage(0, CompatPacket.class, CompatPacket::encode, CompatPacket::decode, CompatPacket::handle);
    }

    public void registerPacket(ResourceLocation resourceLocation) {

    }

    public void registerNetworkReceiverS2C(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        packetsS2C.put(resourceLocation, consumer);
    }

    public void registerNetworkReceiverC2S(ResourceLocation resourceLocation, ServerPlatform.C2SPacketHandler consumer) {
        packetsC2S.put(resourceLocation, consumer);
    }

    public void handle(@Nullable ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        if (player == null) {
            Consumer<FriendlyByteBuf> handlerS2C = packetsS2C.getOrDefault(id, arg -> {});
            handlerS2C.accept(payload);
        } else {
            ServerPlatform.C2SPacketHandler handlerC2S = packetsC2S.getOrDefault(id, (server, player1, arg) -> {});
            handlerC2S.handlePacket(player.getServer(), player, payload);
        }
    }

    public void sendS2C(ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), new CompatPacket(id, payload));
    }

    public void sendC2S(ResourceLocation id, FriendlyByteBuf payload) {
        channel.sendToServer(new CompatPacket(id, payload));
    }
}