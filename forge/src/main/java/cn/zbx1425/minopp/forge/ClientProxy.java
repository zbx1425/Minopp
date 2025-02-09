package cn.zbx1425.minopp.forge;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.gui.GameOverlayLayer;
import cn.zbx1425.minopp.platform.RegistryObject;
import cn.zbx1425.minopp.platform.forge.ClientPlatformImpl;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import cn.zbx1425.minopp.render.EntityAutoPlayerRenderer;
import cn.zbx1425.minopp.render.HandCardsWithoutLevelRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxy {

    public static class ModEventBusListener {
        @SubscribeEvent
        public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("game_overlay", (gui, graphics, partialTick, width, height) -> {
                GameOverlayLayer.INSTANCE.render(graphics, partialTick);
            });
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            for (RegistryObject<KeyMapping> keyMapping : ClientPlatformImpl.KEY_MAPPINGS) {
                KeyMapping key = keyMapping.get();
                key.setKeyConflictContext(NoConflictKeyConflictContext.INSTANCE);
                event.register(key);
            }
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.renderer.item.ItemProperties.register(Mino.ITEM_HAND_CARDS.get(),
                    Mino.id("custom_renderer"), (stack, level, entity, seed) -> 1.0F);
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(Mino.BLOCK_ENTITY_TYPE_MINO_TABLE.get(), BlockEntityMinoTableRenderer::new);
            event.registerEntityRenderer(Mino.ENTITY_AUTO_PLAYER.get(), EntityAutoPlayerRenderer::new);
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onComputeFovModifier(ComputeFovModifierEvent event) {
            event.setNewFovModifier(event.getNewFovModifier() * (float)MinoClient.globalFovModifier);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                MinoClient.tick();
            }
        }
    }

    private static class NoConflictKeyConflictContext implements IKeyConflictContext {

        public static NoConflictKeyConflictContext INSTANCE = new NoConflictKeyConflictContext();

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return false;
        }
    }
}