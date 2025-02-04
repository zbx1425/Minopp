package cn.zbx1425.minopp.platform.forge;

import cn.zbx1425.minopp.forge.MinoForge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CompatPacket {
    public final ResourceLocation id;
    private final FriendlyByteBuf buffer;

    public CompatPacket(ResourceLocation id, FriendlyByteBuf buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    public static void encode(CompatPacket message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.id);
        buffer.writeInt(message.buffer.readableBytes());
        buffer.writeBytes(message.buffer);
        message.buffer.resetReaderIndex();
    }

    public static CompatPacket decode(FriendlyByteBuf buffer) {
        return new CompatPacket(buffer.readResourceLocation(), new FriendlyByteBuf(buffer.readBytes(buffer.readInt())));
    }

    public static void handle(CompatPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MinoForge.PACKET_REGISTRY.commit(ctx.get().getSender(), message.id, message.buffer);
        });
        ctx.get().setPacketHandled(true);
    }
}
