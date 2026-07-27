package cn.zbx1425.minopp.mixin;

import cn.zbx1425.minopp.item.ItemHandCards;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

//? if >=26.1 {
    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    public void onScroll(Inventory instance, int i, Operation<Void> original, @Local(name = "wheel") int wheel) {
        if (ItemHandCards.Client.handleScrollWheel(wheel)) {
            original.call(instance, instance.getSelectedSlot());
        } else {
            original.call(instance, i);
        }
    }
//? }
}
