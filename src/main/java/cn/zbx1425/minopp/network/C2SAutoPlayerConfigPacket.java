package cn.zbx1425.minopp.network;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.platform.multiver.NbtIOShim;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;

public class C2SAutoPlayerConfigPacket {

    public static final Identifier ID = Mino.id("auto_player_config");

    public static void handleC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
        int entityId = packet.readInt();
        boolean shouldDelete = packet.readBoolean();
        CompoundTag config = shouldDelete ? null : packet.readNbt();

        server.execute(() -> {
            if (player.level().getEntity(entityId) instanceof EntityAutoPlayer autoPlayer) {
                if (autoPlayer.getConfigEditRestricted() && !PlayerShim.hasPermissions(player, 2)) return;
                if (shouldDelete) {
                    autoPlayer.dropFromLootTable(
                        player.level(),
                        new DamageSource(player.level().registryAccess()
                            .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.PLAYER_ATTACK), player),
                        true,
                        autoPlayer.getLootTable().orElseThrow()
                    );
                    autoPlayer.remove(Entity.RemovalReason.KILLED);
                } else {
                    autoPlayer.applyConfig(NbtIOShim.decode(EntityAutoPlayer.Config.CODEC, config));
                }
            }
        });
    }

    public static class Client {
        public static void sendC2S(EntityAutoPlayer autoPlayer) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeInt(autoPlayer.getId());
            packet.writeBoolean(false); // Not deleting
            packet.writeNbt(NbtIOShim.encode(EntityAutoPlayer.Config.CODEC, autoPlayer.getConfig()));
            ClientPlatform.sendPacketToServer(ID, packet);
        }

        public static void sendDeleteC2S(EntityAutoPlayer autoPlayer) {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeInt(autoPlayer.getId());
            packet.writeBoolean(true); // Deleting
            ClientPlatform.sendPacketToServer(ID, packet);
        }
    }
} 