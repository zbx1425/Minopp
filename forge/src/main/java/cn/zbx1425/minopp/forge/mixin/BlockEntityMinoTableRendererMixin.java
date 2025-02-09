package cn.zbx1425.minopp.forge.mixin;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.render.BlockEntityMinoTableRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.IBlockEntityRendererExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityMinoTableRenderer.class)
public abstract class BlockEntityMinoTableRendererMixin implements IBlockEntityRendererExtension<BlockEntityMinoTable> {

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull BlockEntityMinoTable blockEntity) {
        return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
