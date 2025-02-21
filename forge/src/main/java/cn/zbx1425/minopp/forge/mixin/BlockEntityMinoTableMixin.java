package cn.zbx1425.minopp.forge.mixin;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityMinoTable.class)
public abstract class BlockEntityMinoTableMixin implements IForgeBlockEntity {

    @Override
    public @NotNull AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
