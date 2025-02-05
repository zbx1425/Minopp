package cn.zbx1425.minopp.forge.compat.touhou_little_maid;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class MaidExtraBrain implements IExtraMaidBrain {
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return List.of(MemoryTypeRegister.TARGET_POS.get());
    }
}
