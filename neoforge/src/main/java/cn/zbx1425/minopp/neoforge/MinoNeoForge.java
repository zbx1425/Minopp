package cn.zbx1425.minopp.neoforge;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.platform.neoforge.CompatPacketRegistry;
import cn.zbx1425.minopp.platform.neoforge.RegistriesWrapperImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Mino.MOD_ID)
public final class MinoNeoForge {

    private static final RegistriesWrapperImpl registries = new RegistriesWrapperImpl();
    public static final CompatPacketRegistry PACKET_REGISTRY = new CompatPacketRegistry();

    public MinoNeoForge(IEventBus eventBus) {
        Mino.init(registries);

        registries.registerAllDeferred(eventBus);
        eventBus.register(RegistriesWrapperImpl.RegisterCreativeTabs.class);
        eventBus.register(ModEventBusListener.class);
        if (FMLEnvironment.dist.isClient()) {
            MinoClient.init();
            eventBus.register(ClientProxy.ModEventBusListener.class);
            NeoForge.EVENT_BUS.register(ClientProxy.ForgeEventBusListener.class);
        }
    }

    public static class ModEventBusListener {

        @SubscribeEvent
        public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            MinoNeoForge.PACKET_REGISTRY.commit(registrar);
        }
    }
}
