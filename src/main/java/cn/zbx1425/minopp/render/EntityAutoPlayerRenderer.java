package cn.zbx1425.minopp.render;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=26.1 {
import net.minecraft.client.renderer.PlayerSkinRenderCache;
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

//? if <1.20.2 {
/*import net.minecraft.client.model.PlayerModel;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
*///? } else if <26.1 {
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

    //? if >=26.1
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public EntityAutoPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
        slimModel = model;
        wideModel = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false);
        //? if <26.1
        //this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        //? if >=26.1
        this.addLayer(new PlayerItemInHandLayer<>(this));
        //? if >=26.1
        this.playerSkinRenderCache = context.getPlayerSkinRenderCache();
    }

    @Override
    //? if <26.1
    //public Identifier getTextureLocation(EntityAutoPlayer entity) {
    //? if >=26.1
    public Identifier getTextureLocation(AvatarRenderState state) {
        //? if <1.20.2 {
        /*Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkinLocation(result.get());
        }
        return Mino.vanillaId("textures/entity/player/slim/alex.png");
        *///? } else if <26.1 {
        /*Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkin(result.get()).texture();
        }
        return Mino.vanillaId("textures/entity/player/slim/alex.png");
        *///? } else {
        return state.skin.body().texturePath();
        //? }
    }

    @Override
    //? if <26.1
    //public void render(EntityAutoPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    //? if >=26.1
    public void submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        //? if <1.20.2 {
        /*Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        model = wideModel;
        if (result.isPresent()) {
            var info = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(result.get());
            if (info.containsKey(MinecraftProfileTexture.Type.SKIN)
                    && "slim".equals(info.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model"))) {
                model = slimModel;
            }
        } else {
            model = slimModel;
        }
        PlayerModel playerModel = this.getModel();
        playerModel.setAllVisible(true);
        ItemStack handStack = entity.getMainHandItem();
        playerModel.rightArmPose = !handStack.isEmpty() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        *///? } else if <26.1 {
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
        model = state.skin.model() == PlayerModelType.SLIM ? slimModel : wideModel;
        super.submit(state, poseStack, submitNodeCollector, camera);
        //? }
    }

    //? if <1.20.2 {
    /*public static Identifier resolveClientSkinTexture(EntityAutoPlayer entity) {
        Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            return Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(result.get());
        }
        return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
    }
    *///? } else if <26.1 {
    /*public static PlayerSkin resolveClientSkin(EntityAutoPlayer entity) {
        Optional<GameProfile> result = entity.clientSkinGameProfile.getNow(Optional.empty());
        if (result.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkin(result.get());
        }
        return DefaultPlayerSkin.get(entity.getUUID());
    }

    public static Identifier resolveClientSkinTexture(EntityAutoPlayer entity) {
        return resolveClientSkin(entity).texture();
    }
    *///? } else {
    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    public PlayerSkin resolveClientSkin(EntityAutoPlayer entity) {
        return this.playerSkinRenderCache.getOrDefault(entity.clientSkinGameProfile).playerSkin();
    }

    public Identifier resolveClientSkinTexture(EntityAutoPlayer entity) {
        return resolveClientSkin(entity).body().texturePath();
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
