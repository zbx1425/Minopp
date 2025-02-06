package cn.zbx1425.minopp.forge.compat.touhou_little_maid.task;

import cn.zbx1425.minopp.Mino;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MinoppTask implements IMaidTask {
    public static final ResourceLocation UID = new ResourceLocation(Mino.MOD_ID, "minopp_task");
    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Mino.ITEM_HAND_CARDS.get().getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_IDLE.get(), 0.5f);
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        return Lists.newArrayList(Pair.of(5, new FindMinoTask(0.6f, 2)));
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5, new PlayMinoTask()));
    }

//    Unsupported in 1.20.1
//    @Override
//    public boolean canSitInJoy(@NotNull EntityMaid maid, String joyType) {
//        return joyType.equals(MinoTable);
//    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Collections.singletonList(Pair.of("has_mino", this::hasMino));
    }

    private boolean hasMino(EntityMaid maid) {
        return maid.getMainHandItem().isEmpty() || maid.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get());
    }
}
