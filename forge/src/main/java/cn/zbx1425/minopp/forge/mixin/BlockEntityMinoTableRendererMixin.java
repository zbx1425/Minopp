package cn.zbx1425.minopp.forge.mixin;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityMinoTableRenderer.class)
public abstract class BlockEntityMinoTableRendererMixin implements BlockEntityRenderer<BlockEntityMinoTable>, IForgeBlockEntity {

    public BlockEntityMinoTableRendererMixin(BlockEntityRendererProvider.Context ctx) {
        super();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
