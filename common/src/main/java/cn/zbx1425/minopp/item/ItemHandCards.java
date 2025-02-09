package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.platform.GroupedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

public class ItemHandCards extends GroupedItem {
    
    private static final String NBT_TABLE_POS = "TablePos";
    private static final String NBT_BEARER_ID = "BearerId";
    private static final String NBT_CLIENT_HAND_INDEX = "ClientHandIndex";
    
    public ItemHandCards() {
        super(() -> null, p -> p.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (usedHand != InteractionHand.MAIN_HAND) return super.use(level, player, usedHand);
        BlockPos gamePos = getHandCardGamePos(player);
        if (gamePos != null && level.getBlockEntity(gamePos) instanceof BlockEntityMinoTable tableEntity) {
            if (tableEntity.game != null && tableEntity.getPlayersList().stream().anyMatch(p -> p.uuid.equals(player.getGameProfile().getId()))) {
                // Card is valid
                return InteractionResultHolder.fail(player.getItemInHand(usedHand));
            }
        }
        // Game table not found, card no longer usable
        player.setItemInHand(usedHand, ItemStack.EMPTY);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    public static CardPlayer getCardPlayer(Player player) {
        return new CardPlayer(player);
    }

    public static BlockPos getHandCardGamePos(Player player) {
        if (!player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) return null;
        CompoundTag tag = player.getMainHandItem().getTag();
        if (tag == null || !tag.contains(NBT_TABLE_POS)) return null;
        BlockPos tablePos = NbtUtils.readBlockPos(tag.getCompound(NBT_TABLE_POS));
        BlockState blockState = player.level().getBlockState(tablePos);
        if (!blockState.is(Mino.BLOCK_MINO_TABLE.get())) return null;
        return BlockMinoTable.getCore(blockState, tablePos);
    }

    public static int getClientHandIndex(Player player) {
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            CompoundTag tag = player.getMainHandItem().getTag();
            return tag != null ? tag.getInt(NBT_CLIENT_HAND_INDEX) : 0;
        } else {
            return 0;
        }
    }

    public static void setCardGameBinding(ItemStack stack, BlockPos tablePos, UUID bearerId) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(NBT_TABLE_POS, NbtUtils.writeBlockPos(tablePos));
        tag.putUUID(NBT_BEARER_ID, bearerId);
    }

    public static void setClientHandIndex(ItemStack stack, int index) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_CLIENT_HAND_INDEX, index);
    }

    public static CardGameBinding getCardGameBinding(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_TABLE_POS) || !tag.contains(NBT_BEARER_ID)) return null;
        return new CardGameBinding(
            NbtUtils.readBlockPos(tag.getCompound(NBT_TABLE_POS)),
            tag.getUUID(NBT_BEARER_ID)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CardGameBinding binding = getCardGameBinding(stack);
        if (binding != null) {
            tooltipComponents.add(Component.literal("Table: " + binding.tablePos().toShortString()));
            if (Minecraft.getInstance().player != null && binding.bearerId().equals(Minecraft.getInstance().player.getGameProfile().getId())) {
                tooltipComponents.add(Component.literal("NOT YOUR CARD!").withStyle(ChatFormatting.RED));
            }
        }
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public record CardGameBinding(BlockPos tablePos, UUID bearerId) {
        // BearerId is to convey holder info into BEWLR
    }
}
