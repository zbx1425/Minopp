package cn.zbx1425.minopp.mixin;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.item.ItemHandCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    void swapPaint(double direction, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack holding = player.getMainHandItem();
        if (holding.is(Mino.ITEM_HAND_CARDS.get())) {
            if (holding.getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(),
                    ItemHandCards.CardGameBindingComponent.EMPTY).tablePos().isEmpty()) return;
            int handIndex = holding.getOrDefault(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), 0);
            holding.set(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), Math.max(0, handIndex - (int)Math.signum(direction)));
            ci.cancel();
        }
    }
}
