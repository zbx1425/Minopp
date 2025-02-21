package cn.zbx1425.minopp.forge.compat.touhou_little_maid;

import cn.zbx1425.minopp.forge.compat.touhou_little_maid.entity.MaidEntitySit;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntitySitRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class TouhouLittleMaidCompat {

    public static void init(IEventBus eventBus) {
        boolean installed = LoadingModList.get().getModFileById(TouhouLittleMaid.MOD_ID) != null;
        if (installed) {
            PoiRegistry.POI_TYPES.register(eventBus);
            MemoryTypeRegister.MEMORY_MODULE_TYPES.register(eventBus);

            eventBus.register(new TouhouLittleMaidCompat());
            if (FMLEnvironment.dist.isClient()) {
                eventBus.register(ClientRegister.class);
            }
        }
    }

    private static class ClientRegister {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(MaidEntitySit.TYPE, EntitySitRenderer::new);
        }
    }

    @SubscribeEvent
    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> helper.register("maid_enitiy_sit", MaidEntitySit.TYPE));
    }
}
