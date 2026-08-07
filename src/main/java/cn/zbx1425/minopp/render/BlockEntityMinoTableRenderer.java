package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.platform.RegistryObject;
import cn.zbx1425.minopp.platform.multiver.RenderShim;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
//? if >=26.1 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
//? }
//? if <26.1
//import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

//? if <26.1 {
/*import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.joml.Matrix4f;
*///? } else {

//? }

import java.util.Random;

//? if <26.1
//public class BlockEntityMinoTableRenderer implements BlockEntityRenderer<BlockEntityMinoTable> {
//? if >=26.1
public class BlockEntityMinoTableRenderer implements BlockEntityRenderer<BlockEntityMinoTable, BlockEntityMinoTableRenderer.MinoTableRenderState> {

//? if <26.1 {
    /*private ItemRenderer itemRenderer;

    public BlockEntityMinoTableRenderer(BlockEntityRendererProvider.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
    }
*///? } else {
    private final ItemModelResolver itemRenderer;

    public BlockEntityMinoTableRenderer(BlockEntityRendererProvider.Context ctx) {
        itemRenderer = ctx.itemModelResolver();
    }
//? }


    @Override
//? if <26.1 {
    /*public void render(BlockEntityMinoTable blockEntity, float f, PoseStack poseStack,
                       MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
*///? } else {
    public void submit(BlockEntityMinoTableRenderer.@NonNull MinoTableRenderState state, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector sink, @NonNull CameraRenderState camera) {
//? }
        //? if >=26.1
        BlockEntityMinoTable blockEntity = state.blockEntity;

        if (blockEntity.game == null) return;

        if (BlockMinoTable.Client.isCursorHittingPile()) {
        //? if <26.1 {
            /*LevelRenderer.renderLineBox(poseStack, multiBufferSource.getBuffer(RenderType.lines()),
                    BlockMinoTable.Client.getPileAabb(blockEntity), 1, 1, 0, 1f);
        *///? } else {
              sink.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                  RenderShim.renderLineBox(pose, buffer,
                      BlockMinoTable.Client.getPileAabb(blockEntity), 1, 1, 0, 1f);
              });
        //? }
        }

        poseStack.pushPose();
        poseStack.translate(0.4, 0.94, 0.4);
        poseStack.scale(0.4f, 0.3f, 0.4f);
        //? if <26.1
        //BakedModel model = itemRenderer.getModel(HAND_CARDS_MODEL_PLACEHOLDER.get(), null, null, 0);

        poseStack.mulPose(Axis.XP.rotation(-(float)Math.PI / 2));
        Random deckRandom = new Random(1);

        // Internally, CardGame allows deck to be briefly empty before it tries to re-shuffle the cards.
        // Add one here to make the deck always visible for better UX.
        int logicalDeckSize = blockEntity.game.deck.size() + 1;

        for (int ci = 0; ci < Math.ceil(logicalDeckSize / 5f); ci++) {
            poseStack.pushPose();
            poseStack.translate((ci % 3 - 1) * 0.02, 0, ci / 16f + 0.01);
            if (ci == Math.ceil(logicalDeckSize / 5f) - 1) {
                float topCardThicknessRatio =( ((logicalDeckSize - 1) % 5) + 1) * (1 / 5f);
                poseStack.translate(0, 0, -(0.5f - topCardThicknessRatio / 2) / 16f);
                poseStack.scale(1, 1, topCardThicknessRatio);
            }
            //? if <26.1 {
            /*itemRenderer.render(HAND_CARDS_MODEL_PLACEHOLDER.get(), ItemDisplayContext.FIXED, false,
                    poseStack, multiBufferSource, packedLight, packedOverlay, model);
            *///? } else if >=26.1 {
            state.cardItemModel.submit(poseStack, sink, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            //? }
            poseStack.popPose();
        }
        poseStack.popPose();

        //? if <26.1 {
        /*VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(Mino.id("textures/gui/deck.png")));
        PoseStack matrices = poseStack;
        *///? } else if >=26.1 {
        int packedLight = state.lightCoords;
        sink.submitCustomGeometry(poseStack, RenderTypes.entityCutout(Mino.id("textures/gui/deck.png")),
            (pose, vertexConsumer) -> {
                PoseStack matrices = new PoseStack();
                matrices.mulPose(pose.pose());
        //? }


        matrices.pushPose();
        matrices.translate(1.1, 0.9 + 1 / 16f, 1.1);
        Random discardRandom = new Random(1);
        for (int ci = 0; ci <= blockEntity.game.discardDeck.size(); ci++) {
            final float HALF_SPREAD = 0.45f;
            matrices.pushPose();
            matrices.translate((discardRandom.nextFloat() * 2 - 1) * HALF_SPREAD, ci * 0.001f, (discardRandom.nextFloat() * 2 - 1) * HALF_SPREAD);
            matrices.pushPose();
            matrices.mulPose(Axis.YP.rotation(discardRandom.nextFloat() * 2 * (float)Math.PI));
            matrices.mulPose(Axis.XP.rotation(-(float)Math.PI / 2));
            matrices.scale(0.2f, 0.2f, 0.2f);

            Card card = ci == blockEntity.game.discardDeck.size() ? blockEntity.game.topCard : blockEntity.game.discardDeck.get(ci);
            float cardU = switch (card.family) {
                case NUMBER -> Math.abs(card.number) * 16;
                case SKIP -> 160;
                case DRAW -> 176;
                case REVERSE -> 192;
            } / 256f;
            float cardV = card.suit.ordinal() * 25 / 128f;
            float cardUW = 16 / 256f;
            float cardVH = 25 / 128f;
            int color = (ci == blockEntity.game.discardDeck.size())
                    ? 0xFFFFFFFF : 0xFFAAAAAA;
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), -0.52f, 0.8f, 0, 0, 0, 1, cardU, cardV, OverlayTexture.NO_OVERLAY, packedLight, 0xFF000000);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), -0.52f, -0.8f, 0, 0, 0, 1, cardU, cardV + cardVH, OverlayTexture.NO_OVERLAY, packedLight, 0xFF000000);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), 0.52f, -0.8f, 0, 0, 0, 1, cardU + cardUW, cardV + cardVH, OverlayTexture.NO_OVERLAY, packedLight, 0xFF000000);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), 0.52f, 0.8f, 0, 0, 0, 1, cardU + cardUW, cardV, OverlayTexture.NO_OVERLAY, packedLight, 0xFF000000);
            matrices.translate(0, 0, 1 / 64f);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), -0.5f, 0.78f, 0, 0, 0, 1, cardU, cardV, OverlayTexture.NO_OVERLAY, packedLight, color);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), -0.5f, -0.78f, 0, 0, 0, 1, cardU, cardV + cardVH, OverlayTexture.NO_OVERLAY, packedLight, color);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), 0.5f, -0.78f, 0, 0, 0, 1, cardU + cardUW, cardV + cardVH, OverlayTexture.NO_OVERLAY, packedLight, color);
            RenderShim.fillVertEntity(vertexConsumer, matrices.last(), 0.5f, 0.78f, 0, 0, 0, 1, cardU + cardUW, cardV, OverlayTexture.NO_OVERLAY, packedLight, color);

            if (ci == blockEntity.game.discardDeck.size()) {
                Component cardText = (card.suit == Card.Suit.WILD)
                    ? card.getDisplayName().copy().append(" ").append(Component.translatable("game.minopp.card.suit." + card.getEquivSuit().name().toLowerCase()))
                    : card.getDisplayName();
                //? if <26.1 {
                /*matrices.popPose();
                matrices.pushPose();
                matrices.translate(0, 0.2f, 0);
                matrices.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                matrices.scale(0.4f, 0.4f, 0.4f);
                matrices.scale(0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = matrices.last().pose();
                Font font = Minecraft.getInstance().font;
                float bgOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int bgColor = (int)(bgOpacity * 255.0F) << 24;
                float textX = (float)(-font.width(cardText) / 2);
                font.drawInBatch(cardText, textX, 0, 553648127, false, matrix4f, multiBufferSource, Font.DisplayMode.SEE_THROUGH, bgColor, LightTexture.FULL_BRIGHT);
                font.drawInBatch(cardText, textX, 0, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                *///? } else {
                matrices.popPose();
                matrices.pushPose();
                matrices.scale(0.4f, 0.4f, 0.4f);
                double distToTableCenterSqr = camera.pos.distanceToSqr(Vec3.atLowerCornerWithOffset(blockEntity.getBlockPos(), 1, 1, 1));
                sink.submitNameTag(matrices, Vec3.ZERO, 0, cardText,
                    distToTableCenterSqr <= 5 * 5,
                    LightCoordsUtil.FULL_BRIGHT, distToTableCenterSqr, camera
                );
                //? }
            }
            matrices.popPose();
            matrices.popPose();
        }
        matrices.popPose();

        //? if >=26.1
        });
    }

    @Override
    //? if <26.1
    //public boolean shouldRenderOffScreen(BlockEntityMinoTable blockEntity) {
    //? if >=26.1
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private static final RegistryObject<ItemStack> HAND_CARDS_MODEL_PLACEHOLDER = new RegistryObject<>(() -> new ItemStack(Mino.ITEM_HAND_CARDS_NO_BEWLR.get()));

    //? if >=26.1 {

    @Override
    public @NonNull MinoTableRenderState createRenderState() {
        return new MinoTableRenderState();
    }

    @Override
    public void extractRenderState(@NonNull BlockEntityMinoTable blockEntity, @NonNull MinoTableRenderState state, float partialTicks,
                                   @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;

        itemRenderer.updateForTopItem(
            state.cardItemModel, HAND_CARDS_MODEL_PLACEHOLDER.get(),
            ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0
        );
    }

    public static class MinoTableRenderState extends BlockEntityRenderState {

        // TODO Do the extraction
        public BlockEntityMinoTable blockEntity;

        public ItemStackRenderState cardItemModel = new ItemStackRenderState();
    }
//? }
}
