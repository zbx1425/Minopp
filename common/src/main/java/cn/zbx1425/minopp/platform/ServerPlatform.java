package cn.zbx1425.minopp.platform;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class ServerPlatform {

    @ExpectPlatform
    public static boolean isFabric() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntitySupplier<T> supplier, Block block) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPacket(ResourceLocation resourceLocation) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerNetworkReceiver(ResourceLocation resourceLocation, C2SPacketHandler packetCallback) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPlayerJoinEvent(Consumer<ServerPlayer> consumer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPlayerQuitEvent(Consumer<ServerPlayer> consumer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerServerStartingEvent(Consumer<MinecraftServer> consumer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerServerStoppingEvent(Consumer<MinecraftServer> consumer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerTickEvent(Consumer<MinecraftServer> consumer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendPacketToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf packet) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface C2SPacketHandler {

        void handlePacket(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet);
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        T supplier(BlockPos pos, BlockState state);
    }
}
