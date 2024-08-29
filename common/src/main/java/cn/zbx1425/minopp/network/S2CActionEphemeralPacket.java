package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.ActionMessage;
import cn.zbx1425.minopp.platform.ServerPlatform;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class S2CActionEphemeralPacket {

    public static final ResourceLocation ID = Mino.id("action_ephemeral");

    public static void sendS2C(ServerPlayer target, BlockPos gamePos, ActionMessage message) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBlockPos(gamePos);
        if (message == null) {
            packet.writeBoolean(false);
        } else {
            packet.writeBoolean(true);
            packet.writeNbt(message.toTag());
        }
        ServerPlatform.sendPacketToPlayer(target, ID, packet);
    }

    public static class Client {

        public static void handleS2C(FriendlyByteBuf packet) {
            BlockPos gamePos = packet.readBlockPos();
            ActionMessage message = packet.readBoolean() ? new ActionMessage(Objects.requireNonNull(packet.readNbt())) : null;
            if (Minecraft.getInstance().level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity) {
                tableEntity.clientEphemeral = message;
            }
        }
    }
}
