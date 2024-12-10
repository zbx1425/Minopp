package cn.zbx1425.minopp.neoforge;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.gui.GameOverlayLayer;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.platform.neoforge.ClientPlatformImpl;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import cn.zbx1425.minopp.render.EntityAutoPlayerRenderer;
import cn.zbx1425.minopp.render.HandCardsWithoutLevelRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public class ClientProxy {

    public static class ModEventBusListener {
        @SubscribeEvent
        public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.SCOREBOARD_SIDEBAR, Mino.id("game_overlay"), GameOverlayLayer.INSTANCE);
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            for (KeyMapping keyMapping : ClientPlatformImpl.KEY_MAPPINGS) {
                keyMapping.setKeyConflictContext(NoConflictKeyConflictContext.INSTANCE);
                event.register(keyMapping);
            }
        }

        @SubscribeEvent
        public static void onRegisterClientExtension(RegisterClientExtensionsEvent event) {
            event.registerItem(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return HandCardsWithoutLevelRenderer.INSTANCE.get();
                }
            }, Mino.ITEM_HAND_CARDS.get());
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
            event.setNewFovModifier(event.getNewFovModifier() * (float)ClientPlatform.globalFovModifier);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            MinoClient.tick();
        }

    }

    private static class NoConflictKeyConflictContext implements IKeyConflictContext {

        public static NoConflictKeyConflictContext INSTANCE = new NoConflictKeyConflictContext();

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext iKeyConflictContext) {
            return false;
        }
    }
}