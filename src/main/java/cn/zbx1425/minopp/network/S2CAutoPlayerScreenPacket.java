package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import cn.zbx1425.minopp.gui.AutoPlayerScreen;
import cn.zbx1425.minopp.platform.ServerPlatform;
import cn.zbx1425.minopp.platform.multiver.NbtIOShim;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class S2CAutoPlayerScreenPacket {

    public static final Identifier ID = Mino.id("auto_player_screen");
    private static Boolean yaclAvailable = null;

    private static boolean isYaclAvailable() {
        if (yaclAvailable == null) {
            try {
                Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib");
                yaclAvailable = true;
            } catch (ClassNotFoundException e) {
                yaclAvailable = false;
            }
        }
        return yaclAvailable;
    }

    public static void sendS2C(ServerPlayer target, EntityAutoPlayer autoPlayer) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeInt(autoPlayer.getId());
        packet.writeNbt(NbtIOShim.pourOne(autoPlayer::writeConfigToTag));
        ServerPlatform.sendPacketToPlayer(target, ID, packet);
    }

    public static class Client {

        public static void handleS2C(FriendlyByteBuf packet) {
            int entityId = packet.readInt();
            CompoundTag config = packet.readNbt();

            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().level.getEntity(entityId) instanceof EntityAutoPlayer autoPlayer) {
                    NbtIOShim.topUpOne(autoPlayer::readConfigFromTag, config);
                    if (isYaclAvailable()) {
                        Minecraft.getInstance().setScreen(AutoPlayerScreen.create(autoPlayer, Minecraft.getInstance().screen));
                    } else {
                        PlayerShim.sendSystemMessage(Minecraft.getInstance().player,
                            Component.translatable("gui.minopp.bot_config.yacl_missing"));
                    }
                }
            });
        }
    }
} 