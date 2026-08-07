package cn.zbx1425.minopp.fabric;
//? if fabric {

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.game.client.shard.ShardResources;
import cn.zbx1425.minopp.gui.GameOverlayLayer;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import cn.zbx1425.minopp.render.EntityAutoPlayerRenderer;
//? if >=26.1
import cn.zbx1425.minopp.render.HandCardsSpecialRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

//? if <26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import cn.zbx1425.minopp.render.HandCardsWithoutLevelRenderer;
*///? } else {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
//? }
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class MinoFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        MinoClient.init();
        MinoFabric.PACKET_REGISTRY.commitClient();

        //? if <26.1
        //HudRenderCallback.EVENT.register(GameOverlayLayer.INSTANCE::render);
        //? if >=26.1
        HudElementRegistry.attachElementAfter(VanillaHudElements.SCOREBOARD, Mino.id("hand_card_overlay"), GameOverlayLayer.INSTANCE::render);

        //? if <26.1
        //BuiltinItemRendererRegistry.INSTANCE.register(Mino.ITEM_HAND_CARDS.get(), HandCardsWithoutLevelRenderer.INSTANCE.get()::renderByItem);

        BlockEntityRenderers.register(Mino.BLOCK_ENTITY_TYPE_MINO_TABLE.get(), BlockEntityMinoTableRenderer::new);
        EntityRendererRegistry.register(Mino.ENTITY_AUTO_PLAYER.get(), EntityAutoPlayerRenderer::new);

        ClientTickEvents.START_CLIENT_TICK.register(event -> MinoClient.tick());

        //? if >=26.1
        SpecialModelRenderers.ID_MAPPER.put(Mino.id("hand_cards_bewlr"), HandCardsSpecialRenderer.Unbaked.MAP_CODEC);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Mino.id("resources");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                ShardResources.INSTANCE.onResourceReload();
            }
        });
    }
}

//? }
