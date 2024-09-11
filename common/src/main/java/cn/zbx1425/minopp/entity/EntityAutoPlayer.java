package cn.zbx1425.minopp.entity;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.ActionReport;
import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    public ActionReport performAI(CardGame game, CardPlayer realPlayer) {
        Card topCard = game.topCard;
        boolean forgetsMino = new Random().nextFloat() < 0.2;
        boolean shoutsMino = !forgetsMino && realPlayer.hand.size() <= 2;
        // If we have a card of same number but different suit
        if (topCard.number != -1) { // Don't apply this logic to Wild
            for (Card card : realPlayer.hand) {
                if (card.number == topCard.number && card.suit != topCard.getEquivSuit()) {
                    ActionReport result = game.playCard(realPlayer, card, null, shoutsMino);
                    if (!result.isFail) return result;
                }
            }
        }
        // If we have a card of same suit
        for (Card card : realPlayer.hand) {
            if (card.suit == topCard.getEquivSuit()) {
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

        // Rate limiting
        if (level().isClientSide) return;
        if (level().getGameTime() - lastTickGameTime < 10) {
            return;
        }
        lastTickGameTime = level().getGameTime();
        if (!hasCustomName()) return;

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
                                cardPlayer = new CardPlayer(uuid, getCustomName().getString());
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
                    CardPlayer realPlayer = tableEntity.game.deAmputate(cardPlayer);
                    ActionReport result = performAI(tableEntity.game, realPlayer);
                    tableEntity.handleActionResult(result, realPlayer, null);
                    entityData.set(GAME_END_TICK, -1L);
                } else {
                    isThinking = false;
                }
            } else {
                setInvulnerable(false);
                if (entityData.get(GAME_END_TICK) == -1L) {
                    entityData.set(GAME_END_TICK, level().getGameTime() + 100);
                }
                if (level().getGameTime() - entityData.get(GAME_END_TICK) <= 20 * 6) {
                    if (onGround()) jumpFromGround();
                }
            }
        } else {
            cardPlayer = null;
            entityData.set(HAND_STACK, ItemStack.EMPTY);
            tablePos = null;
        }
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
        if (tablePos != null) {
            compound.putLong("tablePos", tablePos.asLong());
        }
        compound.put("cardPlayer", cardPlayer.toTag());
        compound.put("handStack", entityData.get(HAND_STACK).save(level().registryAccess(), new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("tablePos")) {
            tablePos = BlockPos.of(compound.getLong("tablePos"));
        } else {
            tablePos = null;
        }
        if (compound.contains("cardPlayer")) {
            cardPlayer = new CardPlayer(compound.getCompound("cardPlayer"));
        }
        if (compound.contains("handStack")) {
            entityData.set(HAND_STACK, ItemStack.parse(level().registryAccess(), compound.getCompound("handStack")).orElse(ItemStack.EMPTY));
        }
    }

    private static final EntityDataAccessor<ItemStack> HAND_STACK = SynchedEntityData.defineId(EntityAutoPlayer.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Long> GAME_END_TICK = SynchedEntityData.defineId(EntityAutoPlayer.class, EntityDataSerializers.LONG);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAND_STACK, ItemStack.EMPTY);
        builder.define(GAME_END_TICK, 0L);
    }
}
