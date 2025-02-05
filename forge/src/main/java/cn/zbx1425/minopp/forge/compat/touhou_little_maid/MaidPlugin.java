package cn.zbx1425.minopp.forge.compat.touhou_little_maid;

import cn.zbx1425.minopp.forge.compat.touhou_little_maid.task.MinoppTask;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new MinoppTask());
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new MaidExtraBrain());
    }
}
