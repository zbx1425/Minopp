package cn.zbx1425.minopp.neoforge;
//? if forgelike {

/*import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.game.client.shard.ShardResources;
import cn.zbx1425.minopp.gui.GameOverlayLayer;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.platform.RegistryObject;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import cn.zbx1425.minopp.render.EntityAutoPlayerRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
//? if >=26.1
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
//? if >=1.21
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
//? if >=1.21
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
//? if >=1.21
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
//? if <1.21
//import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
//? if <1.21
//import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

//? if >=1.21.2
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

//? if <26.1 {
/^import cn.zbx1425.minopp.render.HandCardsWithoutLevelRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
^///? } else if >=26.1 {
import cn.zbx1425.minopp.render.HandCardsSpecialRenderer;
//? }

public class ClientProxy {

    public static class ModEventBusListener {
        @SubscribeEvent
        //? if >=1.21 {
        public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.SCOREBOARD_SIDEBAR, Mino.id("game_overlay"), GameOverlayLayer.INSTANCE);
        }
        //? } else {
        /^public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.SCOREBOARD.id(), "game_overlay", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                GameOverlayLayer.INSTANCE.render(guiGraphics, partialTick);
            });
        }
        ^///? }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            for (RegistryObject<KeyMapping> keyMapping : ClientPlatform.KEY_MAPPINGS) {
                KeyMapping key = keyMapping.get();
                key.setKeyConflictContext(NoConflictKeyConflictContext.INSTANCE);
                event.register(key);
            }
        }

        //? if >=1.21 {
        @SubscribeEvent
        public static void onRegisterClientExtension(RegisterClientExtensionsEvent event) {
            //? if <26.1 {
            /^event.registerItem(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return HandCardsWithoutLevelRenderer.INSTANCE.get();
                }
            }, Mino.ITEM_HAND_CARDS.get());
            ^///? }
        }
        //? }

        //? if >=26.1 {
        @SubscribeEvent
        public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
            event.register(
                Mino.id("hand_cards_bewlr"),
                HandCardsSpecialRenderer.Unbaked.MAP_CODEC
            );
        }
        //? }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(Mino.BLOCK_ENTITY_TYPE_MINO_TABLE.get(), BlockEntityMinoTableRenderer::new);
            event.registerEntityRenderer(Mino.ENTITY_AUTO_PLAYER.get(), EntityAutoPlayerRenderer::new);
        }

        //? if >= 1.21.2 {
        @SubscribeEvent
        private static void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
            MinoNeoForge.PACKET_REGISTRY.commitClient(event);
        }
        //? }

        @SubscribeEvent
        //? if >=1.21 {
        public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event) {
        //? } else {
        /^public static void onTextureAtlasStitched(TextureStitchEvent.Post event) {
        ^///? }
            //? if <26.1 {
            /^if (event.getAtlas() == Minecraft.getInstance().getModelManager().getAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)) {
                ShardResources.INSTANCE.onResourceReload();
            }
            ^///? } else {
            if (event.getAtlas() == Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS)) {
                ShardResources.INSTANCE.onResourceReload();
            }
            //? }
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onComputeFovModifier(ComputeFovModifierEvent event) {
            event.setNewFovModifier(event.getNewFovModifier() * (float)MinoClient.globalFovModifier);
        }

        @SubscribeEvent
        //? if >=1.21 {
        public static void onClientTick(ClientTickEvent.Pre event) {
            MinoClient.tick();
        }
        //? } else {
        /^public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                MinoClient.tick();
            }
        }
        ^///? }

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

*///?}
