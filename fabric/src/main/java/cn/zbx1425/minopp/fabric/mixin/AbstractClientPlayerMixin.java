package cn.zbx1425.minopp.fabric.mixin;

import cn.zbx1425.minopp.MinoClient;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    public void getFieldOfViewModifier(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * (float) MinoClient.globalFovModifier);
    }
}
