package cn.zbx1425.minopp.render;

//? if >=26.1 {

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.platform.RegistryObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class HandCardsSpecialRenderer implements SpecialModelRenderer<HandCardsSpecialRenderer.HandCardRenderState> {

    @Override
    public void submit(HandCardsSpecialRenderer.@Nullable HandCardRenderState state, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector sink, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
            if (state == null) return;

            poseStack.pushPose();
            poseStack.translate(0.4, 0.65, 0.65);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-20));
            poseStack.mulPose(Axis.XP.rotationDegrees(30));

            for (int k = 0; k < state.hand.size(); k++) {
                state.cardItemModel.submit(poseStack, sink, packedLight, packedOverlay, 0);
                poseStack.translate(0, 0.1, 0.08);
            }

            // Render arrow texture
            if (state.isOwnerCurrentlyInTurn) {
                poseStack.pushPose();
                poseStack.translate(0, 0.3, 0.3);
                // Transform must be somehow messed up but it works so I'm not going to fix it
                poseStack.mulPose(Axis.XP.rotationDegrees(-110f));
                sink.submitCustomGeometry(poseStack, RenderTypes.entityCutout(Mino.id("textures/gui/arrow_down.png")),
                    (pose, buffer) -> {
                        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
                        Matrix3f cameraRotMat = new Matrix3f();
                        camera.rotation().get(cameraRotMat);
                        pose.pose().set3x3(cameraRotMat).scale(0.2f, 0.2f, 1);
                        pose.normal().identity();
                        float v0 = ((int) (System.currentTimeMillis() / 100L) % 5) * 0.2f;
                        float v1 = v0 + 0.2f;
                        buffer
                            .addVertex(pose, -1, 1, 0).setNormal(pose, 0, 1, 0)
                            .setUv(0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFFFFFFFF)
                            .addVertex(pose, -1, -1, 0).setNormal(pose, 0, 1, 0)
                            .setUv(0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFFFFFFFF)
                            .addVertex(pose, 1, -1, 0).setNormal(pose, 0, 1, 0)
                            .setUv(1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFFFFFFFF)
                            .addVertex(pose, 1, 1, 0).setNormal(pose, 0, 1, 0)
                            .setUv(1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setColor(0xFFFFFFFF);
                    });
                poseStack.popPose();
            }

            poseStack.popPose();
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> output) {

    }

    private static final RegistryObject<ItemStack> HAND_CARDS_MODEL_PLACEHOLDER = new RegistryObject<>(() -> new ItemStack(Mino.ITEM_HAND_CARDS_NO_BEWLR.get()));

    @Override
    public @Nullable HandCardRenderState extractArgument(@NonNull ItemStack stack) {
        ItemHandCards.CardGameBindingComponent gameBinding = stack.get(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get());
        if (gameBinding == null) return null;
        BlockPos tablePos = gameBinding.tablePos();
        ClientLevel level = Minecraft.getInstance().level;
        BlockState blockState = level.getBlockState(tablePos);
        tablePos = BlockMinoTable.getCore(blockState, tablePos);
        if (!(level.getBlockEntity(tablePos) instanceof BlockEntityMinoTable tableEntity)) return null;
        if (tableEntity.game == null) return null;
        CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.uuid.equals(gameBinding.bearerId()))
            .findFirst().orElse(null);
        if (realPlayer == null) return null;

        ItemStackRenderState cardItemModel = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(cardItemModel,
            HAND_CARDS_MODEL_PLACEHOLDER.get(), ItemDisplayContext.NONE, null, null, 0);

        return new HandCardRenderState(
            List.copyOf(realPlayer.hand),
            tableEntity.game.currentPlayerIndex == tableEntity.game.players.indexOf(realPlayer),
            cardItemModel
        );
    }

    public record HandCardRenderState(
        List<Card> hand,
        boolean isOwnerCurrentlyInTurn,
        ItemStackRenderState cardItemModel
    ) {

    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<HandCardRenderState> {

        public static final MapCodec<HandCardsSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(HandCardsSpecialRenderer.Unbaked::new);

        @Override
        public @NonNull SpecialModelRenderer<HandCardRenderState> bake(@NonNull BakingContext context) {
            return new HandCardsSpecialRenderer();
        }

        @Override
        public @NonNull MapCodec<? extends SpecialModelRenderer.Unbaked<HandCardRenderState>> type() {
            return MAP_CODEC;
        }
    }
}

//? }
