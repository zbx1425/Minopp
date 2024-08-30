package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.platform.RegistryObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class BlockEntityMinoTableRenderer implements BlockEntityRenderer<BlockEntityMinoTable> {

    private static final RegistryObject<ItemStack> HAND_CARDS_MODEL_PLACEHOLDER = new RegistryObject<>(() -> new ItemStack(Mino.ITEM_HAND_CARDS_MODEL_PLACEHOLDER.get()));

    private ItemRenderer itemRenderer;

    public BlockEntityMinoTableRenderer(BlockEntityRendererProvider.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(BlockEntityMinoTable blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        if (blockEntity.game == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.9, 0.5);
        poseStack.scale(0.5f, 0.3f, 0.5f);
        BakedModel model = itemRenderer.getModel(HAND_CARDS_MODEL_PLACEHOLDER.get(), null, null, 0);
        poseStack.mulPose(Axis.XP.rotation(-(float)Math.PI / 2));
        Random deckRandom = new Random(0);
        for (int ci = 0; ci < blockEntity.game.deck.size() / 5; ci++) {
            poseStack.translate(deckRandom.nextFloat() * 0.1 - 0.05, deckRandom.nextFloat() * 0.1 - 0.05, 1 / 16f);
            itemRenderer.render(HAND_CARDS_MODEL_PLACEHOLDER.get(), ItemDisplayContext.FIXED, false,
                    poseStack, multiBufferSource, i, j, model);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(1, 0.9 + 1 / 16f, 1);
        poseStack.scale(0.5f, 0.2f, 0.5f);
        poseStack.mulPose(Axis.XP.rotation(-(float)Math.PI / 2));
        Random discardRandom = new Random(1);
        for (int ci = 0; ci < blockEntity.game.discardDeck.size(); ci++) {
            poseStack.pushPose();
            poseStack.translate(discardRandom.nextFloat() * 1.5 - 0.75, discardRandom.nextFloat() * 1.5 - 0.75, discardRandom.nextFloat() * 1 / 32f);
            poseStack.mulPose(Axis.ZP.rotation(discardRandom.nextFloat() * 2 * (float)Math.PI));
            itemRenderer.render(HAND_CARDS_MODEL_PLACEHOLDER.get(), ItemDisplayContext.FIXED, false,
                    poseStack, multiBufferSource, i, (ci == blockEntity.game.discardDeck.size() - 1) ? 655360 : j, model);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
