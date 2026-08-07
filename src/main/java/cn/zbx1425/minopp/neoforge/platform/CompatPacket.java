package cn.zbx1425.minopp.neoforge.platform;
//? if forgelike && >=1.21 {

/*import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class CompatPacket {

    public final Identifier id;

    public CompatPacket(Identifier id) {
        this.id = id;
        this.TYPE = new CustomPacketPayload.Type<>(id);
    }

    public class Payload implements CustomPacketPayload {

        public final FriendlyByteBuf buffer;

        public Payload(FriendlyByteBuf buffer) {
            this.buffer = buffer;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public final CustomPacketPayload.Type<Payload> TYPE;

    public final StreamCodec<ByteBuf, Payload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf dest, Payload src) {
            dest.writeBytes(src.buffer, 0, src.buffer.writerIndex());
        }

        @Override
        public Payload decode(ByteBuf src) {
            ByteBuf data;
            if (src.isDirect()) {
                data = Unpooled.buffer(src.readableBytes(), src.readableBytes());
                src.readBytes(data);
            } else {
                data = src.retainedDuplicate();
                src.readerIndex(src.readerIndex() + src.readableBytes());
            }
            return new Payload(new FriendlyByteBuf(data));
        }
    };
}

*///?}
