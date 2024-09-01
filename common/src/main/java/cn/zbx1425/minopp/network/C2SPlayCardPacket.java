package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.ActionMessage;
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
import net.minecraft.world.level.block.state.BlockState;

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
        final UUID doubtTargetPlayerUuid;
        switch (actionType) {
            case 0 -> {
                card = new Card(Objects.requireNonNull(packet.readNbt()));
                wildSelectionOrdinal = packet.readInt();
                doubtTargetPlayerUuid = null;
            }
            case 3 -> {
                card = null;
                wildSelectionOrdinal = -1;
                doubtTargetPlayerUuid = packet.readUUID();
            }
            default -> {
                card = null;
                wildSelectionOrdinal = -1;
                doubtTargetPlayerUuid = null;
            }
        }

        server.execute(() -> {
            if (level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity) {
                if (tableEntity.game == null) return;
                CardPlayer cardPlayer = tableEntity.game.deAmputate(playerUuid);
                if (cardPlayer == null) return;
                ActionMessage result;
                switch (actionType) {
                    case 0 -> {
                        Card.Suit wildSelection = wildSelectionOrdinal == -1 ? null : Card.Suit.values()[wildSelectionOrdinal];
                        result = tableEntity.game.playCard(cardPlayer, card, wildSelection);
                    }
                    case 1 -> result = tableEntity.game.playNoCard(cardPlayer);
                    case 2 -> result = tableEntity.game.shoutMino(cardPlayer);
                    case 3 -> result = tableEntity.game.doubtMino(cardPlayer, doubtTargetPlayerUuid);
                    default -> result = ActionMessage.NO_GAME;
                }
                tableEntity.handleActionResult(result, player);
            }
        });
    }

    public static class Client {

        public static void sendPlayCardC2S(BlockPos gamePos, CardPlayer player, Card card, Card.Suit wildSelection) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeUUID(player.uuid);
            packet.writeInt(0);
            packet.writeNbt(card.toTag());
            packet.writeInt(wildSelection == null ? -1 : wildSelection.ordinal());
            ClientPlatform.sendPacketToServer(ID, packet);
        }

        public static void sendPlayNoCardC2S(BlockPos gamePos, CardPlayer player) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeUUID(player.uuid);
            packet.writeInt(1);
            ClientPlatform.sendPacketToServer(ID, packet);
        }

        public static void sendShoutMinoC2S(BlockPos gamePos, CardPlayer player) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeUUID(player.uuid);
            packet.writeInt(2);
            ClientPlatform.sendPacketToServer(ID, packet);
        }

        public static void sendDoubtMinoC2S(BlockPos gamePos, CardPlayer srcPlayer, CardPlayer targetPlayer) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeUUID(srcPlayer.uuid);
            packet.writeInt(3);
            packet.writeUUID(targetPlayer.uuid);
            ClientPlatform.sendPacketToServer(ID, packet);
        }
    }
}
