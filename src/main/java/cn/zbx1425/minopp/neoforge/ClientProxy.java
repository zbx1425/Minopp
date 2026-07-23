package cn.zbx1425.minopp.neoforge;
//? if neoforge {

/*import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.game.client.shard.ShardResources;
import cn.zbx1425.minopp.gui.GameOverlayLayer;
//? if neoforge && 1.21.1
//import cn.zbx1425.minopp.neoforge.compat.signmeup.MinimapVisibility;
import cn.zbx1425.minopp.platform.ClientPlatform;
import cn.zbx1425.minopp.platform.RegistryObject;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import cn.zbx1425.minopp.render.EntityAutoPlayerRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
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
        public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.SCOREBOARD_SIDEBAR, Mino.id("game_overlay"), GameOverlayLayer.INSTANCE);
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            for (RegistryObject<KeyMapping> keyMapping : ClientPlatform.KEY_MAPPINGS) {
                KeyMapping key = keyMapping.get();
                key.setKeyConflictContext(NoConflictKeyConflictContext.INSTANCE);
                event.register(key);
            }
        }

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
        public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event) {
            if (event.getAtlas() == Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS)) {
                ShardResources.INSTANCE.onResourceReload();
            }
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onComputeFovModifier(ComputeFovModifierEvent event) {
            event.setNewFovModifier(event.getNewFovModifier() * (float)MinoClient.globalFovModifier);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            MinoClient.tick();

            //? if neoforge && 1.21.1
            //MinimapVisibility.tick();
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

*///?}
