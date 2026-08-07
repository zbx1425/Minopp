package cn.zbx1425.minopp.platform.multiver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;

public class RenderShim {

    public static void renderLineBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, float red, float green, float blue, float alpha) {
        renderLineBox(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
    }

    public static void renderLineBox(PoseStack.Pose pose, VertexConsumer consumer,
             double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
             float red, float green, float blue, float alpha) {
        float f = (float)minX;
        float g = (float)minY;
        float h = (float)minZ;
        float i = (float)maxX;
        float j = (float)maxY;
        float k = (float)maxZ;
        fillVertLine(consumer, pose, f, g, h, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, i, g, h, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, f, g, h, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        fillVertLine(consumer, pose, f, j, h, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        fillVertLine(consumer, pose, f, g, h, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        fillVertLine(consumer, pose, f, g, k, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        fillVertLine(consumer, pose, i, g, h, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        fillVertLine(consumer, pose, i, j, h, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        fillVertLine(consumer, pose, i, j, h, red, green, blue, alpha, -1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, f, j, h, red, green, blue, alpha, -1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, f, j, h, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        fillVertLine(consumer, pose, f, j, k, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        fillVertLine(consumer, pose, f, j, k, red, green, blue, alpha, 0.0f, -1.0f, 0.0f);
        fillVertLine(consumer, pose, f, g, k, red, green, blue, alpha, 0.0f, -1.0f, 0.0f);
        fillVertLine(consumer, pose, f, g, k, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, i, g, k, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, i, g, k, red, green, blue, alpha, 0.0f, 0.0f, -1.0f);
        fillVertLine(consumer, pose, i, g, h, red, green, blue, alpha, 0.0f, 0.0f, -1.0f);
        fillVertLine(consumer, pose, f, j, k, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, i, j, k, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        fillVertLine(consumer, pose, i, g, k, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        fillVertLine(consumer, pose, i, j, k, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        fillVertLine(consumer, pose, i, j, h, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        fillVertLine(consumer, pose, i, j, k, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
    }

    private static void fillVertLine(VertexConsumer buffer,
                                     PoseStack.Pose pose, float x, float y, float z,
                                     float r, float g, float b, float a,
                                     float nx, float ny, float nz) {
        //? if <1.21 {
        /*buffer.vertex(pose.pose(), x, y, z)
            .color(r, g, b, a)
            .normal(pose.normal(), nx, ny, nz)
            .endVertex();
        *///? } else if <26.1
        //buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        //? if >=26.1
        buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(4.0f);
    }

    public static void fillVertEntity(VertexConsumer buffer, PoseStack.Pose pose,
                                      float x, float y, float z, float nx, float ny, float nz,
                                      float u, float v, int overlay, int light, int color) {
        //? if <1.21 {
        /*int ca = (color >> 24) & 0xFF;
        int cr = (color >> 16) & 0xFF;
        int cg = (color >> 8) & 0xFF;
        int cb = color & 0xFF;
        buffer.vertex(pose.pose(), x, y, z)
            .color(cr, cg, cb, ca)
            .uv(u, v)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(pose.normal(), nx, ny, nz)
            .endVertex();
        *///? } else {
        buffer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
        //? }
    }
}
