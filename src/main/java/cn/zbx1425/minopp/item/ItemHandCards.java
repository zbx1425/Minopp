package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.gui.TurnDeadMan;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

//? if <26.1
//import net.minecraft.world.InteractionResultHolder;
//? if >=26.1
import net.minecraft.world.InteractionResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemHandCards extends GroupedItem {
    
    public ItemHandCards() {
        super(p -> p.stacksTo(1), Mino.id("hand_cards"), () -> null);
    }

    @Override
    //~ if >=26.1 'InteractionResultHolder<ItemStack>' -> 'InteractionResult'
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if (usedHand != InteractionHand.MAIN_HAND) return super.use(level, player, usedHand);
        BlockPos gamePos = getHandCardGamePos(player);
        if (gamePos != null && level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity) {
            if (tableEntity.game != null && tableEntity.getPlayersList().stream().anyMatch(p -> p.uuid.equals(PlayerShim.getGameProfileId(player)))) {
                // Card is valid
                //? if <26.1
                //return InteractionResultHolder.fail(player.getItemInHand(usedHand));
                //? if >=26.1
                return InteractionResult.FAIL;
            }
        }
        // Game table not found, card no longer usable
        player.setItemInHand(usedHand, ItemStack.EMPTY);
        //? if <26.1
        //return InteractionResultHolder.consume(player.getItemInHand(usedHand));
        //? if >=26.1
        return InteractionResult.CONSUME;
    }

    public static CardPlayer getCardPlayer(Player player) {
        return new CardPlayer(player);
    }

    public static BlockPos getHandCardGamePos(Player player) {
        if (!player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) return null;
        CardGameBindingComponent binding = player.getMainHandItem().get(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get());
        if (binding == null) return null;
        BlockPos tablePos = binding.tablePos();
        BlockState blockState = player.level().getBlockState(tablePos);
        if (!blockState.is(Mino.BLOCK_MINO_TABLE.get())) return null;
        return BlockMinoTable.getCore(blockState, tablePos);
    }

    public static int getClientHandIndex(Player player) {
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            return player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), 0);
        } else {
            return 0;
        }
    }

    @Override
    //? if <26.1
    //public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    //? if >=26.1
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
        CardGameBindingComponent binding = stack.get(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get());
        //~ if >=26.1 'tooltipComponents.add(' -> 'builder.accept(' {
        if (binding != null) {
            builder.accept(Component.literal("Table: " + binding.tablePos().toShortString()));
            if (!Client.isClientPlayerOwnerOfHandCard(binding)) { // TODO this doesn't seem good, some mod might try to call it from server
                builder.accept(Component.literal("NOT YOUR CARD!").withStyle(ChatFormatting.RED));
            }
        }
        //~ }
        //? if <26.1
        //super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        //? if >=26.1
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
    }

    public record CardGameBindingComponent(BlockPos tablePos, UUID bearerId) {
        // BearerId is to convey holder info into BEWLR

        public static final Codec<CardGameBindingComponent> CODEC = RecordCodecBuilder.create(it -> it.group(
                BlockPos.CODEC.fieldOf("tablePos").orElse(BlockPos.ZERO).forGetter(CardGameBindingComponent::tablePos),
                UUIDUtil.CODEC.fieldOf("bearerId").orElse(Util.NIL_UUID).forGetter(CardGameBindingComponent::bearerId)
        ).apply(it, CardGameBindingComponent::new));

        public static final StreamCodec<ByteBuf, CardGameBindingComponent> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, CardGameBindingComponent::tablePos,
                UUIDUtil.STREAM_CODEC, CardGameBindingComponent::bearerId,
                CardGameBindingComponent::new
        );
    }

    public static class Client {

        public static boolean handleScrollWheel(int direction) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return false;
            ItemStack holding = player.getMainHandItem();
            if (holding.is(Mino.ITEM_HAND_CARDS.get())) {
                BlockPos handCardGamePos = ItemHandCards.getHandCardGamePos(player);
                if (handCardGamePos != null) {
                    if (player.level().getBlockEntity(handCardGamePos) instanceof BlockEntityMinoTable tableEntity) {
                        if (tableEntity.game == null) return false;

                        CardPlayer playerWithoutHand = ItemHandCards.getCardPlayer(player);
                        CardPlayer realPlayer = tableEntity.game.players.stream()
                            .filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
                        if (realPlayer == null) return false;

                        int handIndex = holding.getOrDefault(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), 0);
                        holding.set(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(),
                            Mth.clamp(handIndex - direction, 0, realPlayer.hand.size() - 1));

                        TurnDeadMan.pedal();
                        return true;
                    }
                }
            }
            return false;
        }

        public static boolean isClientPlayerOwnerOfHandCard(CardGameBindingComponent binding) {
            return binding.bearerId().equals(PlayerShim.getGameProfileId(Minecraft.getInstance().player));
        }
    }
}
