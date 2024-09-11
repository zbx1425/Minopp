package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.ActionReport;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.platform.ClientPlatform;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

public class C2SPlayCardPacket {

    public static final ResourceLocation ID = Mino.id("play_card");

    public static void handleC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
        BlockPos gamePos = packet.readBlockPos();
        ServerLevel level = player.serverLevel();
        UUID playerUuid = packet.readUUID();
        int actionType = packet.readInt();
        final Card card;
        final int wildSelectionOrdinal;
        final boolean shout;
        if (actionType == 0) {
            card = new Card(Objects.requireNonNull(packet.readNbt()));
            wildSelectionOrdinal = packet.readInt();
            shout = packet.readBoolean();
        } else {
            card = null;
            wildSelectionOrdinal = -1;
            shout = false;
        }

        server.execute(() -> {
            if (level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity) {
                if (tableEntity.game == null) return;
                CardPlayer cardPlayer = tableEntity.game.deAmputate(playerUuid);
                if (cardPlayer == null) return;
                ActionReport result;
                if (actionType == 0) {
                    Card.Suit wildSelection = wildSelectionOrdinal == -1 ? null : Card.Suit.values()[wildSelectionOrdinal];
                    result = tableEntity.game.playCard(cardPlayer, card, wildSelection, shout);
                } else {
                    result = tableEntity.game.playNoCard(cardPlayer);
                }
                tableEntity.handleActionResult(result, cardPlayer, player);
            }
        });
    }

    public static class Client {

        public static void sendPlayCardC2S(BlockPos gamePos, CardPlayer player, Card card, Card.Suit wildSelection, boolean shout) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeUUID(player.uuid);
            packet.writeInt(0);
            packet.writeNbt(card.toTag());
            packet.writeInt(wildSelection == null ? -1 : wildSelection.ordinal());
            packet.writeBoolean(shout);
            ClientPlatform.sendPacketToServer(ID, packet);
        }

        public static void sendPlayNoCardC2S(BlockPos gamePos, CardPlayer player) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeUUID(player.uuid);
            packet.writeInt(1);
            ClientPlatform.sendPacketToServer(ID, packet);
        }
    }
}
