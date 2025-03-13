package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class EntityAutoPlayerRenderer extends LivingEntityRenderer<EntityAutoPlayer, PlayerModel<EntityAutoPlayer>> {
    private static final String SLIM_NAME = "slim";

    private PlayerModel<EntityAutoPlayer> slimModel;
    private PlayerModel<EntityAutoPlayer> wideModel;

    public EntityAutoPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
        slimModel = model;
        wideModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAutoPlayer entity) {
        Optional<GameProfile> result = entity.clientSkinGameProfile;
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkinLocation(result.get());
        }
        return new ResourceLocation("textures/entity/player/slim/alex.png");
    }

    @Override
    public void render(EntityAutoPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Optional<GameProfile> result = entity.clientSkinGameProfile;
        model = wideModel;
        if (result.isPresent()) {
            var info = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(result.get());
            if (info.containsKey(MinecraftProfileTexture.Type.SKIN) && SLIM_NAME.equals(info.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model"))) {
                model = slimModel;
            }
        } else {
            model = slimModel;
        }

        PlayerModel<EntityAutoPlayer> playerModel = this.getModel();
        playerModel.setAllVisible(true);
        ItemStack handStack = entity.getMainHandItem();
        playerModel.rightArmPose = !handStack.isEmpty() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
