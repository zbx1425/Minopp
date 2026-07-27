package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=26.1 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
//? }
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//? if <26.1
//import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

//? if <26.1 {
/*import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.resources.PlayerSkin;
*///? } else {
import net.minecraft.client.model.player.PlayerModel;
//? }

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//? if <26.1
//public class EntityAutoPlayerRenderer extends LivingEntityRenderer<EntityAutoPlayer, PlayerModel<EntityAutoPlayer>> {
//? if >=26.1
public class EntityAutoPlayerRenderer extends LivingEntityRenderer<EntityAutoPlayer, AvatarRenderState, PlayerModel> {

//~ if >=26.1 'PlayerModel<EntityAutoPlayer>' -> 'PlayerModel' {

    private PlayerModel slimModel;
    private PlayerModel wideModel;

    public EntityAutoPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
        slimModel = model;
        wideModel = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false);
        //? if <26.1
        //this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        //? if >=26.1
        this.addLayer(new PlayerItemInHandLayer<>(this));
    }

    @Override
    //? if <26.1
    //public Identifier getTextureLocation(EntityAutoPlayer entity) {
    //? if >=26.1
    public Identifier getTextureLocation(AvatarRenderState state) {
        //? if <26.1 {
        /*Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkin(result.get()).texture();
        }
        return Identifier.withDefaultNamespace("textures/entity/player/slim/alex.png");
        *///? } else {
        // Note that this function won't get actually called on 26.1, due to a special check that forces
        // vanilla AvatarRenderer on AvatarRenderState in EntityRenderDispatcher.
        // But the vanilla impl will yield the right result basing on what we've filled into AvatarRenderState.
        return state.skin.body().texturePath();
        //? }
    }

    @Override
    //? if <26.1
    //public void render(EntityAutoPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    //? if >=26.1
    public void submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        //? if <26.1 {
        /*Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            model = skinManager.getInsecureSkin(result.get()).model() == PlayerSkin.Model.SLIM ? slimModel : wideModel;
        } else {
            model = slimModel;
        }
        PlayerModel playerModel = this.getModel();
        playerModel.setAllVisible(true);
        ItemStack handStack = entity.getMainHandItem();
        playerModel.rightArmPose = !handStack.isEmpty() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        *///? } else {
        // Note that this function won't get actually called on 26.1, due to a special check that forces
        // vanilla AvatarRenderer on AvatarRenderState in EntityRenderDispatcher.
        // But the vanilla impl will yield the right result basing on what we've filled into AvatarRenderState.
        model = state.skin.model() == PlayerModelType.SLIM ? slimModel : wideModel;
        super.submit(state, poseStack, submitNodeCollector, camera);
        //? }
    }

    //? if <26.1 {
    /*public static PlayerSkin resolveClientSkin(EntityAutoPlayer entity) {
        Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkin(result.get());
        }
        return DefaultPlayerSkin.get(entity.getUUID());
    }
    *///? } else {


    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    public static PlayerSkin resolveClientSkin(EntityAutoPlayer entity) {
        return entity.clientSkinGameProfile.thenCompose(gameProfile ->
                gameProfile.map(gameProfileBang ->
                    Minecraft.getInstance().getSkinManager().get(gameProfileBang)
                ).orElse(CompletableFuture.completedFuture(Optional.empty()))
            )
            .getNow(Optional.empty())
            .orElseGet(() -> DefaultPlayerSkin.get(Util.NIL_UUID));
    }

    @Override
    public void extractRenderState(EntityAutoPlayer entity, AvatarRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.skin = resolveClientSkin(entity);
        ItemStack handStack = entity.getMainHandItem();
        state.rightArmPose = !handStack.isEmpty() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        state.id = entity.getId();
    }

    //? }
}

//~ }
