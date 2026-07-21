package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.TableRuleConfig;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.platform.multiver.NbtIOShim;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class C2STableRuleConfigPacket {

    public static final Identifier ID = Mino.id("table_rule_config");

    public static void handleC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
        BlockPos gamePos = packet.readBlockPos();
        CompoundTag rulesNbt = packet.readNbt();
        ServerLevel level = PlayerShim.serverLevel(player);
        server.execute(() -> {
            if (!(level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity)) return;
            if (tableEntity.game != null) return;
            if (tableEntity.demo) return;
            TableRuleConfig config = NbtIOShim.decodeNullable(TableRuleConfig.CODEC, rulesNbt);
            if (config == null) return;
            tableEntity.rules = config;
            tableEntity.sync();
        });
    }

    public static class Client {

        public static void sendC2S(BlockPos gamePos, TableRuleConfig config) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeBlockPos(gamePos);
            packet.writeNbt(NbtIOShim.encode(TableRuleConfig.CODEC, config));
            ClientPlatform.sendPacketToServer(ID, packet);
        }
    }
}
