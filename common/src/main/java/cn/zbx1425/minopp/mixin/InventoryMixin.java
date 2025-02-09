package cn.zbx1425.minopp.mixin;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.gui.TurnDeadMan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void swapPaint(int fromIndex, int toIndex, CallbackInfo ci) {
        Inventory inventory = (Inventory)(Object)this;
        ItemStack fromStack = inventory.getItem(fromIndex);
        if (!fromStack.is(Mino.ITEM_HAND_CARDS.get())) return;
        CompoundTag binding = fromStack.getOrCreateTagElement("CardGameBinding");
        if (binding == null) return;
        BlockPos tablePos = BlockPos.of(binding.getLong("TablePos"));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.level().getBlockEntity(tablePos) instanceof BlockEntityMinoTable table) {
            CardPlayer realPlayer = null;
            for (CardPlayer p : table.getPlayersList()) {
                if (p.uuid.equals(player.getGameProfile().getId())) {
                    realPlayer = p;
                    break;
                }
            }
            if (realPlayer != null) {
                CompoundTag handIndex = fromStack.getOrCreateTagElement("HandIndex");
                int currentIndex = handIndex.getInt("Index");
                handIndex.putInt("Index", Mth.clamp(currentIndex - (int)Math.signum(toIndex - fromIndex), 0, realPlayer.hand.size() - 1));
                TurnDeadMan.pedal();
                ci.cancel();
            }
        }
    }
}
