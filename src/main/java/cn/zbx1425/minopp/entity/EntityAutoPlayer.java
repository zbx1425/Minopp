package cn.zbx1425.minopp.entity;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.AutoPlayer;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.game.ActionReport;
import cn.zbx1425.minopp.gui.AutoPlayerScreen;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.network.S2CAutoPlayerScreenPacket;
import cn.zbx1425.minopp.platform.multiver.NbtIOShim;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import cn.zbx1425.minopp.platform.multiver.WorldShim;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import net.minecraft.server.level.ServerPlayer;

//? if >=26.1 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//? }

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityAutoPlayer extends LivingEntity {

    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public CardPlayer cardPlayer;
    public BlockPos tablePos = null;
    private boolean noPush;

    public EntityAutoPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    private long lastTickGameTime = 0;
    private boolean isThinking = false;
    private long thinkingFinishTime = 0;
    private long gameEndTime = 0;

    public AutoPlayer autoPlayer = new AutoPlayer();

    public CompletableFuture<Optional<GameProfile>> clientSkinGameProfile = CompletableFuture.completedFuture(Optional.empty());
    public String clientSkinGameProfileValidFor = "";

    @Override
    public void tick() {
        super.tick();

        if (WorldShim.isClientSide(level())) {
            if (!clientSkinGameProfileValidFor.equals(entityData.get(SKIN))) {
                clientSkinGameProfileValidFor = entityData.get(SKIN);
                try {
                    UUID skinAsUUID = UUID.fromString(clientSkinGameProfileValidFor);
                    //? if <26.1 {
                    /*clientSkinGameProfile = SkullBlockEntity.fetchGameProfile(skinAsUUID);
                    *///? } else {
                    clientSkinGameProfile = ResolvableProfile.createUnresolved(skinAsUUID)
                        .resolveProfile(Minecraft.getInstance().services().profileResolver())
                        .thenApply(Optional::of);
                    //? }
                } catch (IllegalArgumentException e) {
                    //? if <26.1 {
                    /*clientSkinGameProfile = SkullBlockEntity.fetchGameProfile(clientSkinGameProfileValidFor);
                    *///? } else {
                    clientSkinGameProfile = ResolvableProfile.createUnresolved(clientSkinGameProfileValidFor)
                        .resolveProfile(Minecraft.getInstance().services().profileResolver())
                        .thenApply(Optional::of);
                    //? }
                }
            }
            return;
        }

        // Rate limiting
        if (!getActive()) {
            tablePos = null;
            heal(10);
            return;
        }
        if (autoPlayer.noDelay < 2 && level().getGameTime() - lastTickGameTime < 10) {
            return;
        }
        lastTickGameTime = level().getGameTime();

        if (tablePos == null) {
            // Search for a table and join it
            boolean tableFound = false;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos tobeTablePos = blockPosition().offset(dx, 0, dz);
                        BlockState tobeTableState = level().getBlockState(tobeTablePos);
                        if (tobeTableState.is(Mino.BLOCK_MINO_TABLE.get())) {
                            BlockPos corePos = BlockMinoTable.getCore(tobeTableState, tobeTablePos);
                            BlockEntity blockEntity = level().getBlockEntity(corePos);
                            if (blockEntity instanceof BlockEntityMinoTable tableEntity) {
                                if (tableEntity.game != null) continue;
                                String playerName = hasCustomName() ? getCustomName().getString() : "MinoBot #" + new Random().nextInt(100, 1000);
                                cardPlayer = new CardPlayer(uuid, playerName);
                                tableEntity.joinPlayerToTable(cardPlayer, position());
                                tablePos = corePos;
                                tableFound = true;
                                ItemStack handStack = new ItemStack(Mino.ITEM_HAND_CARDS.get());
                                handStack.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), new ItemHandCards.CardGameBindingComponent(tablePos, cardPlayer.uuid));
                                entityData.set(HAND_STACK, handStack);
                                break;
                            }
                        }
                    }
                    if (tableFound) break;
                }
                if (tableFound) break;
            }
            if (!tableFound) {
                setActive(false);
                return;
            }
        }

        // In-game logic
        lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atLowerCornerWithOffset(tablePos, 1, 1, 1));
        BlockEntity blockEntity = level().getBlockEntity(tablePos);
        if (blockEntity instanceof BlockEntityMinoTable tableEntity) {
            if (tableEntity.game != null) {
                heal(10);
                if (tableEntity.game.players.get(tableEntity.game.currentPlayerIndex).equals(cardPlayer)) {
                    if (autoPlayer.noDelay > 0) {
                        isThinking = false;
                    } else {
                        if (!isThinking) {
                            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN) {
                                thinkingFinishTime = level().getGameTime() + new Random().nextInt(10, 20);
                            } else {
                                thinkingFinishTime = level().getGameTime() + new Random().nextInt(10, 50);
                            }
                            isThinking = true;
                            return;
                        } else {
                            if (level().getGameTime() >= thinkingFinishTime) {
                                isThinking = false;
                            } else {
                                return;
                            }
                        }
                    }
                    CardPlayer realPlayer = tableEntity.game.deAmputate(cardPlayer);
                    ActionReport result = autoPlayer.playAtGame(tableEntity.game, realPlayer, level().getServer());
                    tableEntity.handleActionResult(result, realPlayer, null);
                    gameEndTime = -1;
                } else {
                    isThinking = false;
                }
            } else {
                if (gameEndTime == -1L) {
                    gameEndTime = level().getGameTime() + 100;
                } else if (level().getGameTime() - gameEndTime <= 20 * 3) {
                    if (onGround()) jumpFromGround();
                } else {
                    if (autoPlayer.startGame && tableEntity.getPlayersList().size() >= 2) {
                        tableEntity.startGame(cardPlayer);
                    }
                }
                if (!tableEntity.getPlayersList().stream().anyMatch(p -> p.equals(cardPlayer))) {
                    // It is not on the table
                    cardPlayer = null;
                    entityData.set(HAND_STACK, ItemStack.EMPTY);
                    tablePos = null;
                }
            }
        } else {
            // Table is gone
            cardPlayer = null;
            entityData.set(HAND_STACK, ItemStack.EMPTY);
            tablePos = null;
        }
    }

    @Override
    //? if <26.1
    //public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
    //? if >=26.1
    public InteractionResult interact(final Player player, final InteractionHand hand, final Vec3 location) {
        if (WorldShim.isClientSide(level())) {
            if (PlayerShim.hasPermissions(player, 2) && player.isShiftKeyDown()) {
                return InteractionResult.SUCCESS;
            } else if (getActive() && !player.isShiftKeyDown()) {
                return InteractionResult.SUCCESS;
            }
        } else {
            if (PlayerShim.hasPermissions(player, 2) && player.isShiftKeyDown()) {
                S2CAutoPlayerScreenPacket.sendS2C((ServerPlayer) player, this);
                return InteractionResult.SUCCESS;
            } else if (!getActive() && !player.isShiftKeyDown()) {
                setActive(true);
                return InteractionResult.SUCCESS;
            }
        }
        //? if <26.1
        //return super.interact(player, hand);
        //? if >=26.1
        return super.interact(player, hand, location);
    }

    //? if <26.1 {
    /*@Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public @NotNull Iterable<ItemStack> getHandSlots() {
        return List.of(entityData.get(HAND_STACK), ItemStack.EMPTY);
    }
    *///? }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return entityData.get(HAND_STACK);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {

    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    //? if <26.1 {
    /*@Override
    public boolean isInvulnerableTo(DamageSource source) {
        return isRemoved() || !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }
    *///? }

    //? if <26.1 {
    /*@Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (tablePos != null) compound.putLong("TablePos", tablePos.asLong());
        if (cardPlayer != null) compound.put("CardPlayer", NbtIOShim.encode(CardPlayer.CODEC, cardPlayer));
        compound.merge(NbtIOShim.encode(Config.CODEC, getConfig()));
    *///? } else if >= 26.1 {
    @SuppressWarnings("deprecation")
    protected void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (tablePos != null) output.putLong("TablePos", tablePos.asLong());
        output.storeNullable("CardPlayer", CardPlayer.CODEC, cardPlayer);
        output.store(Config.MAP_CODEC, getConfig());
    //? }
    }

    //? if <26.1 {
    /*@Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        tablePos = compound.contains("TablePos") ? BlockPos.of(compound.getLong("TablePos")) : null;
        cardPlayer = compound.contains("CardPlayer") ? NbtIOShim.decodeNullable(CardPlayer.CODEC, compound.getCompound("CardPlayer")) : null;
        applyConfig(NbtIOShim.decode(Config.CODEC, compound));
    *///? } else if >=26.1 {
    @SuppressWarnings("deprecation")
    protected void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);
        tablePos = input.getLong("TablePos").map(BlockPos::of).orElse(null);
        cardPlayer = input.read("CardPlayer", CardPlayer.CODEC).orElse(null);
        input.read(Config.MAP_CODEC).ifPresent(this::applyConfig);
    //? }

        // Try fix hand stack
        if (tablePos != null && cardPlayer != null) {
            ItemStack handStack = new ItemStack(Mino.ITEM_HAND_CARDS.get());
            handStack.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), new ItemHandCards.CardGameBindingComponent(tablePos, cardPlayer.uuid));
            entityData.set(HAND_STACK, handStack);
        } else {
            entityData.set(HAND_STACK, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean isPushable() {
        return !noPush;
    }

    private static final EntityDataAccessor<ItemStack> HAND_STACK = SynchedEntityData.defineId(EntityAutoPlayer.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntityAutoPlayer.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(EntityAutoPlayer.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAND_STACK, ItemStack.EMPTY);
        builder.define(ACTIVE, false);
        builder.define(SKIN, "");
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .build();
    }

    public boolean getActive() {
        return entityData.get(ACTIVE);
    }

    public void setActive(boolean active) {
        entityData.set(ACTIVE, active);
    }

    public boolean getNoPush() {
        return noPush;
    }

    public void setNoPush(boolean noPush) {
        this.noPush = noPush;
    }

    public String getSkin() {
        return entityData.get(SKIN);
    }

    public void setSkin(String skin) {
        entityData.set(SKIN, skin);
    }

    public Config getConfig() {
        return new Config(
            getActive(), getNoPush(), getSkin(),
            autoPlayer,
            Optional.ofNullable(this.getCustomName())
        );
    }

    public void applyConfig(Config config) {
        setActive(config.active());
        setNoPush(config.noPush());
        setSkin(config.skin());
        this.autoPlayer = config.aiConfig();
        config.customName().ifPresent(this::setCustomName);
    }

    public record Config(
        boolean active, boolean noPush, String skin,
        AutoPlayer aiConfig,
        Optional<Component> customName
    ) {
        private static final Codec<Component> JSON_STRING_COMPONENT_CODEC = Codec.STRING.xmap(
            s -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(s)).getOrThrow(),
            c -> ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow().toString()
        );

        public static final MapCodec<Config> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("Active", false).forGetter(Config::active),
            Codec.BOOL.optionalFieldOf("NoPush", false).forGetter(Config::noPush),
            Codec.STRING.optionalFieldOf("Skin", "").forGetter(Config::skin),
            AutoPlayer.CODEC.optionalFieldOf("AI", new AutoPlayer()).forGetter(Config::aiConfig),
            JSON_STRING_COMPONENT_CODEC.optionalFieldOf("CustomName").forGetter(Config::customName)
        ).apply(instance, Config::new));

        public static final Codec<Config> CODEC = MAP_CODEC.codec();
    }


    private static class Client {

        public static void openAutoPlayerScreen(EntityAutoPlayer autoPlayer) {
            Minecraft.getInstance().setScreen(AutoPlayerScreen.create(autoPlayer, Minecraft.getInstance().screen));
        }
    }
}

