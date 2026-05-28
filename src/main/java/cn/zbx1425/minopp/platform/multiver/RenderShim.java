package cn.zbx1425.minopp.platform.multiver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;

public class RenderShim {

    public static void renderLineBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, float red, float green, float blue, float alpha) {
        renderLineBox(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha, red, green, blue);
    }

    public static void renderLineBox(PoseStack.Pose pose, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha, float red2, float green2, float blue2) {
        float f = (float)minX;
        float g = (float)minY;
        float h = (float)minZ;
        float i = (float)maxX;
        float j = (float)maxY;
        float k = (float)maxZ;
        consumer.addVertex(pose, f, g, h).setColor(red, green2, blue2, alpha).setNormal(pose, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, i, g, h).setColor(red, green2, blue2, alpha).setNormal(pose, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, f, g, h).setColor(red2, green, blue2, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, f, j, h).setColor(red2, green, blue2, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, f, g, h).setColor(red2, green2, blue, alpha).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, f, g, k).setColor(red2, green2, blue, alpha).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, i, g, h).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, i, j, h).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, i, j, h).setColor(red, green, blue, alpha).setNormal(pose, -1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, f, j, h).setColor(red, green, blue, alpha).setNormal(pose, -1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, f, j, h).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, f, j, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, f, j, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, -1.0f, 0.0f);
        consumer.addVertex(pose, f, g, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, -1.0f, 0.0f);
        consumer.addVertex(pose, f, g, k).setColor(red, green, blue, alpha).setNormal(pose, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, i, g, k).setColor(red, green, blue, alpha).setNormal(pose, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, i, g, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, i, g, h).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, f, j, k).setColor(red, green, blue, alpha).setNormal(pose, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, i, j, k).setColor(red, green, blue, alpha).setNormal(pose, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(pose, i, g, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, i, j, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, i, j, h).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, i, j, k).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 0.0f, 1.0f);
    }
}
