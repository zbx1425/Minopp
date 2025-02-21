package cn.zbx1425.minopp.forge.compat.touhou_little_maid;

import cn.zbx1425.minopp.Mino;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class MemoryTypeRegister {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, Mino.MOD_ID);
    public static Supplier<MemoryModuleType<PositionTracker>> TARGET_POS = MEMORY_MODULE_TYPES.register("mino_pos", () -> new MemoryModuleType<>(Optional.empty()));

}
