package cn.zbx1425.minopp.entity;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.ActionReport;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityAutoPlayer extends LivingEntity {

    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public CardPlayer cardPlayer;
    public BlockPos tablePos = null;

    public EntityAutoPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    private long lastTickGameTime = 0;
    private boolean isThinking = false;
    private long thinkingFinishTime = 0;
    private long gameEndTime = 0;

    public boolean aiNoWin = false;
    public boolean aiNoForget = false;
    public byte aiNoDelay = 0;
    public boolean aiStartGame = false;

    public CompletableFuture<Optional<GameProfile>> clientSkinGameProfile = CompletableFuture.completedFuture(Optional.empty());
    public String clientSkinGameProfileValidFor = "";

    public ActionReport performAI(CardGame game, CardPlayer realPlayer) {
        Card topCard = game.topCard;
        boolean forgetsMino = aiNoForget ? false : new Random().nextFloat() < 0.2;
        boolean shoutsMino = !forgetsMino && realPlayer.hand.size() <= 2;

        if (aiNoWin) {
            if (realPlayer.hand.size() <= 1) {
                return game.playNoCard(realPlayer);
            }
        }

        // If we have a card of same number but different suit
        for (Card card : realPlayer.hand) {
            if (card.number == topCard.number && card.suit != topCard.getEquivSuit() && card.suit != Card.Suit.WILD) {
                ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                if (!result.isFail) return result;
            }
        }
        // If we have a card of same suit
        for (Card card : realPlayer.hand) {
            if (card.suit == topCard.getEquivSuit() && card.suit != Card.Suit.WILD) {
                ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                if (!result.isFail) return result;
            }
        }
        // If we have any other card
        for (Card card : realPlayer.hand) {
            if (card.canPlayOn(topCard)) {
                if (card.suit == Card.Suit.WILD) {
                    // Check which suit is most common in hand
                    int[] suitCount = new int[4];
                    for (Card handCard : realPlayer.hand) {
                        if (handCard.suit != Card.Suit.WILD) {
                            suitCount[handCard.suit.ordinal()]++;
                        }
                    }
                    Card.Suit mostCommonSuit = Card.Suit.values()[new Random().nextInt(0, 4)];
                    for (int i = 1; i < 4; i++) {
                        if (suitCount[i] > suitCount[mostCommonSuit.ordinal()]) {
                            mostCommonSuit = Card.Suit.values()[i];
                        }
                    }
                    ActionReport result = game.playCard(realPlayer, card, mostCommonSuit, shoutsMino);
                    if (!result.isFail) return result;
                } else {
                    ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                    if (!result.isFail) return result;
                }
            }
        }
        // We're out of option
        return game.playNoCard(realPlayer);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            if (!clientSkinGameProfileValidFor.equals(entityData.get(SKIN))) {
                clientSkinGameProfileValidFor = entityData.get(SKIN);
                try {
                    UUID skinAsUUID = UUID.fromString(clientSkinGameProfileValidFor);
                    clientSkinGameProfile = SkullBlockEntity.fetchGameProfile(skinAsUUID);
                } catch (IllegalArgumentException e) {
                    clientSkinGameProfile = SkullBlockEntity.fetchGameProfile(clientSkinGameProfileValidFor);
                }
            }
            return;
        }

        // Rate limiting
        if (!entityData.get(ACTIVE)) {
            return;
        }
        if (aiNoDelay < 2 && level().getGameTime() - lastTickGameTime < 10) {
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
                                String playerName = hasCustomName() ? getCustomName().getString()
                                        : "MinoBot #" + new Random().nextInt(100, 1000);
                                cardPlayer = new CardPlayer(uuid, playerName);
                                ItemStack handStack = new ItemStack(Mino.ITEM_HAND_CARDS.get());
                                handStack.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(),
                                        new ItemHandCards.CardGameBindingComponent(uuid, Optional.of(corePos)));
                                entityData.set(HAND_STACK, handStack);
                                tableEntity.joinPlayerToTable(cardPlayer, position());
                                tablePos = corePos;
                                tableFound = true;
                                break;
                            }
                        }
                    }
                    if (tableFound) break;
                }
                if (tableFound) break;
            }
            if (!tableFound) {
                kill();
                return;
            }
        }

        // In-game logic
        lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atLowerCornerWithOffset(tablePos, 1, 1, 1));
        BlockEntity blockEntity = level().getBlockEntity(tablePos);
        if (blockEntity instanceof BlockEntityMinoTable tableEntity) {
            if (tableEntity.game != null) {
                setInvulnerable(true);
                heal(10);
                if (tableEntity.game.players.get(tableEntity.game.currentPlayerIndex).equals(cardPlayer)) {
                    if (aiNoDelay > 0) {
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
                    ActionReport result = performAI(tableEntity.game, realPlayer);
                    tableEntity.handleActionResult(result, realPlayer, null);
                    gameEndTime = -1;
                } else {
                    isThinking = false;
                }
            } else {
                setInvulnerable(false);
                if (gameEndTime == -1L) {
                    gameEndTime = level().getGameTime() + 100;
                } else if (level().getGameTime() - gameEndTime <= 20 * 3) {
                    if (onGround()) jumpFromGround();
                } else {
                    if (aiStartGame && tableEntity.getPlayersList().size() >= 2
                            && tableEntity.getPlayersList().getFirst().equals(cardPlayer)) {
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
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) {
            if (!entityData.get(ACTIVE)) {
                entityData.set(ACTIVE, true);
            } else {
                if (((Player)source.getEntity()).getMainHandItem().is(net.minecraft.world.item.Items.STICK)) {
                    entityData.set(ACTIVE, false);
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public @NotNull Iterable<ItemStack> getHandSlots() {
        return List.of(entityData.get(HAND_STACK), ItemStack.EMPTY);
    }

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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (tablePos != null) compound.putLong("TablePos", tablePos.asLong());
        if (cardPlayer != null) compound.put("CardPlayer", cardPlayer.toTag());
        if (!entityData.get(HAND_STACK).isEmpty()) compound.put("HandStack", entityData.get(HAND_STACK).save(level().registryAccess(), new CompoundTag()));
        compound.putBoolean("Active", entityData.get(ACTIVE));
        compound.putString("Skin", entityData.get(SKIN));
        CompoundTag aiConfig = new CompoundTag();
        aiConfig.putBoolean("NoWin", aiNoWin);
        aiConfig.putBoolean("NoForget", aiNoForget);
        aiConfig.putByte("NoDelay", aiNoDelay);
        aiConfig.putBoolean("StartGame", aiStartGame);
        compound.put("AI", aiConfig);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TablePos", CompoundTag.TAG_LONG)) {
            tablePos = BlockPos.of(compound.getLong("TablePos"));
        } else {
            tablePos = null;
        }
        if (compound.contains("CardPlayer", CompoundTag.TAG_COMPOUND)) {
            cardPlayer = new CardPlayer(compound.getCompound("CardPlayer"));
        } else {
            cardPlayer = null;
        }
        if (compound.contains("HandStack", CompoundTag.TAG_COMPOUND)) {
            entityData.set(HAND_STACK, ItemStack.parse(level().registryAccess(), compound.getCompound("HandStack")).orElse(ItemStack.EMPTY));
        }
        if (compound.contains("Active", CompoundTag.TAG_BYTE)) {
            entityData.set(ACTIVE, compound.getBoolean("Active"));
        }
        if (compound.contains("Skin", CompoundTag.TAG_STRING)) {
            entityData.set(SKIN, compound.getString("Skin"));
        }
        if (compound.contains("AI", CompoundTag.TAG_COMPOUND)) {
            CompoundTag aiConfig = compound.getCompound("AI");
            aiNoWin = aiConfig.getBoolean("NoWin");
            aiNoForget = aiConfig.getBoolean("NoForget");
            aiNoDelay = aiConfig.getByte("NoDelay");
            aiStartGame = aiConfig.getBoolean("StartGame");
        }
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
}

