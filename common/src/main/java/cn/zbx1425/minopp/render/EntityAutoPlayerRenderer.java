package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EntityAutoPlayerRenderer extends LivingEntityRenderer<EntityAutoPlayer, PlayerModel<EntityAutoPlayer>> {

    public EntityAutoPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAutoPlayer entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png");
    }

    @Override
    public void render(EntityAutoPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        PlayerModel<EntityAutoPlayer> playerModel = this.getModel();
        playerModel.setAllVisible(true);
        ItemStack handStack = entity.getMainHandItem();
        playerModel.rightArmPose = !handStack.isEmpty() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
