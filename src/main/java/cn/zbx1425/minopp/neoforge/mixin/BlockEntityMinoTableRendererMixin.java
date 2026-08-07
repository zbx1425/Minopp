package cn.zbx1425.minopp.neoforge.mixin;

//? if forgelike && >=1.21 {
/*import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityMinoTableRenderer.class)
public class BlockEntityMinoTableRendererMixin implements IBlockEntityRendererExtension<BlockEntityMinoTable> {

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull BlockEntityMinoTable blockEntity) {
        return AABB.INFINITE;
    }
}
*///?}

//? if forgelike && <1.21 {
/*import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import net.neoforged.neoforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityMinoTable.class)
public abstract class BlockEntityMinoTableRendererMixin implements IForgeBlockEntity {

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
*///? }