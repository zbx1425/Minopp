package cn.zbx1425.minopp.platform;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//? if fabric {
import cn.zbx1425.minopp.fabric.MinoFabric;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? } else if neoforge {
/*import cn.zbx1425.minopp.neoforge.MinoNeoForge;
*///? }

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientPlatform {

    //? if fabric {

    public static void registerKeyBinding(RegistryObject<KeyMapping> keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping.get());
    }

    public static void registerNetworkReceiver(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        MinoFabric.PACKET_REGISTRY.registerNetworkReceiverS2C(resourceLocation, consumer);
    }

    public static void registerPlayerJoinEvent(Consumer<LocalPlayer> consumer) {
        ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
            if (entity == Minecraft.getInstance().player) {
                consumer.accept((LocalPlayer) entity);
            }
        });
    }

    public static void registerTickEvent(Consumer<Minecraft> consumer) {
        ClientTickEvents.START_CLIENT_TICK.register(consumer::accept);
    }

    public static void sendPacketToServer(ResourceLocation id, FriendlyByteBuf packet) {
        MinoFabric.PACKET_REGISTRY.sendC2S(id, packet);
    }

    //? }

    //? if neoforge {

    /*public static List<RegistryObject<KeyMapping>> KEY_MAPPINGS = new ArrayList<>();

    public static void registerKeyBinding(RegistryObject<KeyMapping> keyMapping) {
        KEY_MAPPINGS.add(keyMapping);
    }

    public static void registerNetworkReceiver(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        MinoNeoForge.PACKET_REGISTRY.registerNetworkReceiverS2C(resourceLocation, consumer);
    }

    public static void registerPlayerJoinEvent(Consumer<LocalPlayer> consumer) {
//        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(consumer::accept);
    }

    public static void registerTickEvent(Consumer<Minecraft> consumer) {
//        ClientTickEvent.CLIENT_PRE.register(consumer::accept);
    }

    public static void sendPacketToServer(ResourceLocation id, FriendlyByteBuf packet) {
        packet.readerIndex(0);
        MinoNeoForge.PACKET_REGISTRY.sendC2S(id, packet);
    }

    *///? }
}
