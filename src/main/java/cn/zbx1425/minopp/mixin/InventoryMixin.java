package cn.zbx1425.minopp.mixin;

import cn.zbx1425.minopp.item.ItemHandCards;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    //? if <26.1 {
    /*@Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    void swapPaint(double direction, CallbackInfo ci) {
        if (ItemHandCards.Client.handleScrollWheel((int)Math.signum(direction))) {
            ci.cancel();
        }
    }
    *///? }
}
