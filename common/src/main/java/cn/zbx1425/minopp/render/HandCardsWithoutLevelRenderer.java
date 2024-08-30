package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.platform.RegistryObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class HandCardsWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {

    public static RegistryObject<HandCardsWithoutLevelRenderer> INSTANCE = new RegistryObject<>(() -> new HandCardsWithoutLevelRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));

    public HandCardsWithoutLevelRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    private static final RegistryObject<ItemStack> HAND_CARDS_MODEL_PLACEHOLDER = new RegistryObject<>(() -> new ItemStack(Mino.ITEM_HAND_CARDS_MODEL_PLACEHOLDER.get()));

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        switch (itemDisplayContext) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                return;
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                ItemHandCards.CardGameBindingComponent gameBinding = itemStack.getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), ItemHandCards.CardGameBindingComponent.EMPTY);
                BlockPos tablePos = gameBinding.tablePos().orElse(null);
                if (tablePos == null) return;
                ClientLevel level = Minecraft.getInstance().level;
                BlockState blockState = level.getBlockState(tablePos);
                tablePos = BlockMinoTable.getCore(blockState, tablePos);
                if (level.getBlockEntity(tablePos) instanceof BlockEntityMinoTable tableEntity) {
                    if (tableEntity.game == null) return;
                    CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.uuid.equals(gameBinding.player())).findFirst().orElse(null);
                    if (realPlayer == null) return;
                    poseStack.popPose();
                    poseStack.pushPose();
                    poseStack.translate(0, 0, 0.18);
                    for (int k = 0; k < realPlayer.hand.size(); k++) {
                        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                        itemRenderer.render(HAND_CARDS_MODEL_PLACEHOLDER.get(), itemDisplayContext, false, poseStack, multiBufferSource, i, j,
                                itemRenderer.getModel(HAND_CARDS_MODEL_PLACEHOLDER.get(), null, null, 0));
                        poseStack.translate(0, 0.02, 0.08);
                    }
                    poseStack.popPose();
                    poseStack.pushPose();
                }
            }
            default -> {
                poseStack.popPose();
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                itemRenderer.render(HAND_CARDS_MODEL_PLACEHOLDER.get(), itemDisplayContext, false, poseStack, multiBufferSource, i, j,
                        itemRenderer.getModel(HAND_CARDS_MODEL_PLACEHOLDER.get(), null, null, 0));
                poseStack.pushPose();
            }
        }
    }
}
