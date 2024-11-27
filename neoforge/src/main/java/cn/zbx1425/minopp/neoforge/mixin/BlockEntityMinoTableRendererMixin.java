package cn.zbx1425.minopp.neoforge.mixin;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
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
