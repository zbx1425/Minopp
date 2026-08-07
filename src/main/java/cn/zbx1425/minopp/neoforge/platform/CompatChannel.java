package cn.zbx1425.minopp.neoforge.platform;
//? if forgelike && <1.21 {

/*import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.ServerPlatform;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.function.Consumer;

public class CompatChannel {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Mino.id("main"), () -> PROTOCOL_VERSION,
            s -> true, s -> true
    );

    private final HashMap<Identifier, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    private final HashMap<Identifier, ServerPlatform.C2SPacketHandler> packetsC2S = new HashMap<>();

    public void registerPacket(Identifier id) {
    }

    public void registerNetworkReceiverS2C(Identifier id, Consumer<FriendlyByteBuf> consumer) {
        packetsS2C.put(id, consumer);
    }

    public void registerNetworkReceiverC2S(Identifier id, ServerPlatform.C2SPacketHandler consumer) {
        packetsC2S.put(id, consumer);
    }

    @SuppressWarnings("unchecked")
    public void commitCommon() {
        CHANNEL.registerMessage(0, RawPacketMessage.class,
                RawPacketMessage::encode, RawPacketMessage::decode,
                (msg, ctxSupplier) -> {
                    var ctx = ctxSupplier.get();
                    ctx.enqueueWork(() -> {
                        ServerPlayer sender = ctx.getSender();
                        if (sender != null) {
                            var handler = packetsC2S.get(msg.id);
                            if (handler != null) {
                                handler.handlePacket(sender.getServer(), sender, msg.buffer);
                            }
                        } else {
                            var handler = packetsS2C.get(msg.id);
                            if (handler != null) {
                                handler.accept(msg.buffer);
                            }
                        }
                    });
                    ctx.setPacketHandled(true);
                }
        );
    }

    public void sendS2C(ServerPlayer player, Identifier id, FriendlyByteBuf payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RawPacketMessage(id, payload));
    }

    public void sendC2S(Identifier id, FriendlyByteBuf payload) {
        CHANNEL.sendToServer(new RawPacketMessage(id, payload));
    }

    public static class RawPacketMessage {
        public final Identifier id;
        public final FriendlyByteBuf buffer;

        public RawPacketMessage(Identifier id, FriendlyByteBuf buffer) {
            this.id = id;
            this.buffer = buffer;
        }

        public static void encode(RawPacketMessage msg, FriendlyByteBuf buf) {
            buf.writeIdentifier(msg.id);
            buf.writeBytes(msg.buffer, 0, msg.buffer.writerIndex());
        }

        public static RawPacketMessage decode(FriendlyByteBuf buf) {
            Identifier id = buf.readIdentifier();
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
            buf.readBytes(data, buf.readableBytes());
            return new RawPacketMessage(id, data);
        }
    }
}

*///? }
