package cn.zbx1425.minopp.neoforge.platform;

import cn.zbx1425.minopp.neoforge.MinoNeoForge;
import cn.zbx1425.minopp.platform.ServerPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;

public class ServerPlatformImpl {

    public static boolean isFabric() {
        return false;
    }

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(ServerPlatform.BlockEntitySupplier<T> supplier, Block block) {
        return BlockEntityType.Builder.of(supplier::supplier, block).build(null);
    }

    public static void registerPacket(ResourceLocation resourceLocation) {
        MinoNeoForge.PACKET_REGISTRY.registerPacket(resourceLocation);
    }

    public static void registerNetworkReceiver(ResourceLocation resourceLocation, ServerPlatform.C2SPacketHandler packetCallback) {
        MinoNeoForge.PACKET_REGISTRY.registerNetworkReceiverC2S(resourceLocation, packetCallback);
    }

    public static void registerPlayerJoinEvent(Consumer<ServerPlayer> consumer) {
//        RegistryUtilities.registerPlayerJoinEvent(consumer);
//        RegistryUtilities.registerPlayerChangeDimensionEvent(consumer);
    }

    public static void registerPlayerQuitEvent(Consumer<ServerPlayer> consumer) {
//        RegistryUtilities.registerPlayerQuitEvent(consumer);
    }

    public static void registerServerStartingEvent(Consumer<MinecraftServer> consumer) {
//        RegistryUtilities.registerServerStartingEvent(consumer);
    }

    public static void registerServerStoppingEvent(Consumer<MinecraftServer> consumer) {
//        RegistryUtilities.registerServerStoppingEvent(consumer);
    }

    public static void registerTickEvent(Consumer<MinecraftServer> consumer) {
//        RegistryUtilities.registerTickEvent(consumer);
    }

    public static void sendPacketToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf packet) {
        packet.readerIndex(0);
        MinoNeoForge.PACKET_REGISTRY.sendS2C(player, id, packet);
    }
}