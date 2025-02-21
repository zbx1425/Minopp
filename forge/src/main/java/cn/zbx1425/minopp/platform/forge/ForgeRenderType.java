package cn.zbx1425.minopp.platform.forge;

import cn.zbx1425.minopp.Mino;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ForgeRenderType extends RenderType {
    private static final Function<ResourceLocation, RenderType> CUSTOM_RENDER = Util.memoize((id) -> {
        return RenderType.create(Mino.MOD_ID + "_custom_render", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 0x200000, true, false, CompositeState.builder()
                .setLightmapState(LIGHTMAP)
                .setShaderState(RENDERTYPE_SOLID_SHADER)
                .setTextureState(new TextureStateShard(id, false, false))
                .createCompositeState(true));
    });

    public static RenderType customRender(ResourceLocation id) {
        return CUSTOM_RENDER.apply(id);
    }

    public ForgeRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new UnsupportedOperationException();
    }
}
