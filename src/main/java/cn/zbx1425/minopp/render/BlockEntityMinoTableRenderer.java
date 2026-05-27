package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.platform.RegistryObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

//? if <26.1 {
/*import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
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
              
        //? }
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.94, 0.5);
        poseStack.scale(0.4f, 0.3f, 0.4f);
        //? if <26.1
        //BakedModel model = itemRenderer.getModel(HAND_CARDS_MODEL_PLACEHOLDER.get(), null, null, 0);

        poseStack.mulPose(Axis.XP.rotation(-(float)Math.PI / 2));
        Random deckRandom = new Random(1);
        for (int ci = 0; ci < Math.ceil(blockEntity.game.deck.size() / 5f); ci++) {
            poseStack.pushPose();
            poseStack.translate((ci % 3 - 1) * 0.02, 0, ci / 16f);
            if (ci == Math.ceil(blockEntity.game.deck.size() / 5f) - 1) {
                float topCardThicknessRatio =( ((blockEntity.game.deck.size() - 1) % 5) + 1) * (1 / 5f);
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
        matrices.translate(1, 0.9 + 1 / 16f, 1);
        matrices.scale(0.2f, 0.2f, 0.2f);
        matrices.mulPose(Axis.XP.rotation(-(float)Math.PI / 2));
        Random discardRandom = new Random(1);
        for (int ci = 0; ci <= blockEntity.game.discardDeck.size(); ci++) {
            matrices.pushPose();
            matrices.translate(discardRandom.nextFloat() * 6 - 3, discardRandom.nextFloat() * 6 - 3, ci / 32f);
            matrices.mulPose(Axis.ZP.rotation(discardRandom.nextFloat() * 2 * (float)Math.PI));

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
                    ? 0xFFFFFFFF : 0xFFBBBBBB;
            vertexConsumer
                    .addVertex(matrices.last(), -0.52f, 0.8f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU, cardV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFF000000)
                    .addVertex(matrices.last(), -0.52f, -0.8f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU, cardV + cardVH).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFF000000)
                    .addVertex(matrices.last(), 0.52f, -0.8f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU + cardUW, cardV + cardVH).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFF000000)
                    .addVertex(matrices.last(), 0.52f, 0.8f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU + cardUW, cardV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFF000000);
            matrices.translate(0, 0, 1 / 64f);
            vertexConsumer
                    .addVertex(matrices.last(), -0.5f, 0.78f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU, cardV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(color)
                    .addVertex(matrices.last(), -0.5f, -0.78f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU, cardV + cardVH).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(color)
                    .addVertex(matrices.last(), 0.5f, -0.78f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU + cardUW, cardV + cardVH).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(color)
                    .addVertex(matrices.last(), 0.5f, 0.78f, 0).setNormal(matrices.last(), 0, 0, 1)
                    .setUv(cardU + cardUW, cardV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(color);

            if (ci == blockEntity.game.discardDeck.size()) {
//                itemRenderer.render(HAND_CARDS_ENCHANTED_MODEL_PLACEHOLDER.get(), ItemDisplayContext.FIXED, false,
//                        poseStack, multiBufferSource, i, j, model);
                Font font = Minecraft.getInstance().font;
                matrices.mulPose(Axis.XP.rotation((float)Math.PI / 2));
                matrices.translate(0, 1f, 0);

//                poseStack.translate(0, 0, 1f);
//                poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                matrices.scale(0.03F, -0.03F, 0.03F);
                Matrix4f matrix4f = matrices.last().pose();
                float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int k = (int)(g * 255.0F) << 24;
                Component component = (card.suit == Card.Suit.WILD)
                    ? card.getDisplayName().copy().append(Component.translatable("game.minopp.card.suit." + card.getEquivSuit().name().toLowerCase()))
                    : card.getDisplayName();
                float h = (float)(-font.width(component) / 2);
                //? if <26.1 {
                /*font.drawInBatch(component, h, 0, 553648127, false, matrix4f, multiBufferSource, Font.DisplayMode.SEE_THROUGH, k, LightTexture.FULL_BRIGHT);
                font.drawInBatch(component, h, 0, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                matrices.mulPose(Axis.YP.rotation((float)Math.PI));
                font.drawInBatch(component, h, 0, 553648127, false, matrix4f, multiBufferSource, Font.DisplayMode.SEE_THROUGH, k, LightTexture.FULL_BRIGHT);
                font.drawInBatch(component, h, 0, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                *///? } else {

                //public void drawInBatch(final String str, final float x, final float y, final int color, final boolean dropShadow,
                // final Matrix4fc pose, final MultiBufferSource bufferSource, final DisplayMode displayMode,
                // final int backgroundColor, final int packedLightCoords) {
                //
                // void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow,
                // Font.DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor);

                FormattedCharSequence cardText = component.getVisualOrderText();
                sink.submitText(matrices, h, 0, cardText, false, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT,
                    553648127, k, 0);
                sink.submitText(matrices, h, 0, cardText, false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT,
                    -1, 0, 0);
                matrices.mulPose(Axis.YP.rotation((float)Math.PI));
                sink.submitText(matrices, h, 0, cardText, false, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT,
                    553648127, k, 0);
                sink.submitText(matrices, h, 0, cardText, false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT,
                    -1, 0, 0);
                //? }
            }
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
    private static final RegistryObject<ItemStack> HAND_CARDS_ENCHANTED_MODEL_PLACEHOLDER = new RegistryObject<>(() -> {
        ItemStack stack = new ItemStack(Mino.ITEM_HAND_CARDS_NO_BEWLR.get());
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    });

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
