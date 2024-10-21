package cn.zbx1425.minopp.neoforge.compat.touhou_little_maid.mixin;

import cn.zbx1425.minopp.neoforge.compat.touhou_little_maid.MemoryTypeRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidBrain;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaidBrain.class)
public class MaidBrainMixin {
    @Inject(method = "getMemoryTypes", at = @At("HEAD"), cancellable = true)
    private static void getMemoryTypes(CallbackInfoReturnable<ImmutableList<MemoryModuleType<?>>> cir) {
        ImmutableList<MemoryModuleType<?>> memoryModuleTypes = ImmutableList.of(
                MemoryModuleType.PATH,
                MemoryModuleType.DOORS_TO_CLOSE,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.NEAREST_HOSTILE,
                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                InitEntities.TARGET_POS.get(),
                MemoryTypeRegister.TARGET_POS.get()
        );
        cir.setReturnValue(memoryModuleTypes);
    }
}
