package cn.zbx1425.minopp.mixin;

import cn.zbx1425.minopp.Mino;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
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
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            int handIndex = player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), 0);
            player.getMainHandItem().set(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), Math.max(0, handIndex - (int)Math.signum(direction)));
            ci.cancel();
        }
    }
}
