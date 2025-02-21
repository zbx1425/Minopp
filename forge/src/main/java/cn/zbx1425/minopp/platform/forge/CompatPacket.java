package cn.zbx1425.minopp.platform.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CompatPacket {

    public final ResourceLocation id;

    public CompatPacket(ResourceLocation id) {
        this.id = id;
    }

    public static class Payload {
        public final FriendlyByteBuf buffer;

        public Payload(FriendlyByteBuf buffer) {
            this.buffer = new FriendlyByteBuf(buffer.copy());
        }

        public void encode(FriendlyByteBuf dest) {
            dest.writeBytes(buffer.copy());
        }

        public static Payload decode(FriendlyByteBuf src) {
            return new Payload(src);
        }
    }
}