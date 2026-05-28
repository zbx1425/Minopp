package cn.zbx1425.minopp.platform;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

//? if fabric {
import cn.zbx1425.minopp.fabric.MinoFabric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
//? } else if neoforge {
/*import cn.zbx1425.minopp.neoforge.MinoNeoForge;
*///? }

import java.util.function.Consumer;

public class ServerPlatform {

    //? if fabric {


    public static boolean isFabric() {
        return true;
    }

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(ServerPlatform.BlockEntitySupplier<T> supplier, Block block) {
        //? if <26.1
        // return BlockEntityType.Builder.of(supplier::supplier, block).build(null);
        //? if >=26.1
        return FabricBlockEntityTypeBuilder.create(supplier::supplier, block).build();
    }

    public static void registerPacket(Identifier resourceLocation) {
        MinoFabric.PACKET_REGISTRY.registerPacket(resourceLocation);
    }

    public static void registerNetworkReceiver(Identifier resourceLocation, ServerPlatform.C2SPacketHandler packetCallback) {
        MinoFabric.PACKET_REGISTRY.registerNetworkReceiverC2S(resourceLocation, packetCallback);
    }

    public static void registerPlayerJoinEvent(Consumer<ServerPlayer> consumer) {
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverWorld) -> {
            if (entity instanceof ServerPlayer) {
                consumer.accept((ServerPlayer) entity);
            }
        });
    }

    public static void registerPlayerQuitEvent(Consumer<ServerPlayer> consumer) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> consumer.accept(handler.player));
    }

    public static void registerServerStartingEvent(Consumer<MinecraftServer> consumer) {
        ServerLifecycleEvents.SERVER_STARTING.register(consumer::accept);
    }

    public static void registerServerStoppingEvent(Consumer<MinecraftServer> consumer) {
        ServerLifecycleEvents.SERVER_STOPPING.register(consumer::accept);
    }

    public static void registerTickEvent(Consumer<MinecraftServer> consumer) {
        ServerTickEvents.START_SERVER_TICK.register(consumer::accept);
    }

    public static void sendPacketToPlayer(ServerPlayer player, Identifier id, FriendlyByteBuf packet) {
        MinoFabric.PACKET_REGISTRY.sendS2C(player, id, packet);
    }

    //? }

    //? if neoforge {


    /*public static boolean isFabric() {
        return false;
    }

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(ServerPlatform.BlockEntitySupplier<T> supplier, Block block) {
        //? if <26.1
        //return BlockEntityType.Builder.of(supplier::supplier, block).build(null);
        //? if >=26.1
        return new BlockEntityType<>(supplier::supplier, false, block);
    }

    public static void registerPacket(Identifier resourceLocation) {
        MinoNeoForge.PACKET_REGISTRY.registerPacket(resourceLocation);
    }

    public static void registerNetworkReceiver(Identifier resourceLocation, ServerPlatform.C2SPacketHandler packetCallback) {
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

    public static void sendPacketToPlayer(ServerPlayer player, Identifier id, FriendlyByteBuf packet) {
        packet.readerIndex(0);
        MinoNeoForge.PACKET_REGISTRY.sendS2C(player, id, packet);
    }

    *///? }

    @SuppressWarnings("unchecked")
    public static <T> DataComponentType<T> createDataComponentType(Codec<T> codec, StreamCodec<ByteBuf, T> streamCodec) {
        return (DataComponentType<T>) DataComponentType.builder().persistent((Codec<Object>)codec)
            .networkSynchronized((StreamCodec<? super RegistryFriendlyByteBuf, Object>)streamCodec).build();
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
