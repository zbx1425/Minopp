package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.ActionMessage;
import cn.zbx1425.minopp.platform.ServerPlatform;
import cn.zbx1425.minopp.platform.multiver.NbtIOShim;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class S2CActionEphemeralPacket {

    public static final Identifier ID = Mino.id("action_ephemeral");

    public static void sendS2C(ServerPlayer target, BlockPos gamePos, ActionMessage message) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeBlockPos(gamePos);
        packet.writeNbt(NbtIOShim.pourOne(message::nbtWriteTo));
        ServerPlatform.sendPacketToPlayer(target, ID, packet);
    }

    public static class Client {

        public static void handleS2C(FriendlyByteBuf packet) {
            BlockPos gamePos = packet.readBlockPos();
            ActionMessage message = NbtIOShim.fillOne(ActionMessage::new, packet.readNbt());
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity) {
                    tableEntity.clientMessageList.add(new Pair<>(message, System.currentTimeMillis() + 8000));
                }
            });
        }
    }
}
