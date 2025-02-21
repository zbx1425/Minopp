package cn.zbx1425.minopp.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ItemDataUtils {
    public static final String MINO_DATA = "minopp_data";

    public static final String CARD_GAME_BINDING_TABLE_POS = "card_game_binding_table_pos";
    public static final String CARD_GAME_BINDING_BEARER_ID = "card_game_binding_bearer_id";
    public static final String CLIENT_HAND_INDEX = "client_hand_index";

    @NotNull
    public static CompoundTag getDataMap(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(MINO_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(MINO_DATA);
        }
        CompoundTag dataMap = new CompoundTag();
        tag.put(MINO_DATA, dataMap);
        return dataMap;
    }

    @Nullable
    public static CompoundTag getDataMapIfPresent(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(MINO_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(MINO_DATA);
        }
        return null;
    }

    public static boolean hasData(ItemStack stack, String key, int type) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap != null && dataMap.contains(key, type);
    }

    public static void removeData(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null) {
            dataMap.remove(key);
            if (dataMap.isEmpty()) {
                stack.removeTagKey(MINO_DATA);
            }
        }
    }

    public static int getHandIndex(ItemStack stack) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getInt(CLIENT_HAND_INDEX);
    }

    public static BlockPos getBlockPos(ItemStack stack) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? null : BlockPos.of(dataMap.getLong(CARD_GAME_BINDING_TABLE_POS));
    }

    @Nullable
    public static UUID getBearerId(ItemStack stack) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.hasUUID(CARD_GAME_BINDING_BEARER_ID)) {
            return dataMap.getUUID(CARD_GAME_BINDING_BEARER_ID);
        }
        return null;
    }

    public static void setHandIndex(ItemStack stack, int index) {
        getDataMap(stack).putInt(CLIENT_HAND_INDEX, index);
    }

    public static void setBlockPos(ItemStack stack, BlockPos blockPos) {
        getDataMap(stack).putLong(CARD_GAME_BINDING_TABLE_POS, blockPos.asLong());
    }

    public static void setBearerId(ItemStack stack, @Nullable UUID uuid) {
        if (uuid == null) {
            removeData(stack, CARD_GAME_BINDING_BEARER_ID);
        } else {
            getDataMap(stack).putUUID(CARD_GAME_BINDING_BEARER_ID, uuid);
        }
    }

    public static void setCardGameBinding(ItemStack stack, BlockPos blockPos, UUID uuid) {
        setBlockPos(stack, blockPos);
        setBearerId(stack, uuid);
    }

}
