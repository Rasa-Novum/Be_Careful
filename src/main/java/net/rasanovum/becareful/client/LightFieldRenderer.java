package net.rasanovum.becareful.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.light.ClientLightFieldState;
import net.rasanovum.becareful.light.LightField;
import org.joml.Matrix4f;

public final class LightFieldRenderer {
    private static final int SHELL_RINGS = 12;
    private static final int SHELL_SEGMENTS = 24;
    private static final float SHELL_ALPHA = 0.10F;
    private static final float LIGHT_R = 1.0F;
    private static final float LIGHT_G = 0.78F;
    private static final float LIGHT_B = 0.20F;

    private LightFieldRenderer() {}

    public static void render(PoseStack poseStack, ClientLevel level, Player player, float tickDelta, Frustum frustum) {
        if (ClientLightFieldState.get().isEmpty()) return;

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        long gameTime = level.getGameTime();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (LightField field : ClientLightFieldState.get()) {
            if (field.expiresAt() <= gameTime) continue;

            Vec3 center = Vec3.atCenterOf(field.center());
            float radius = field.radius();
            AABB bounds = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );
            if (frustum != null && !frustum.isVisible(bounds)) continue;

            poseStack.pushPose();
            poseStack.translate(center.x, center.y, center.z);
            drawSphere(poseStack, radius, SHELL_RINGS, SHELL_SEGMENTS, SHELL_ALPHA);
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        ItemStack totem = new ItemStack(BeCareful.TOTEM_OF_LIGHT);
        float rotation = (level.getGameTime() + tickDelta) * 0.025F;
        for (LightField field : ClientLightFieldState.get()) {
            if (field.expiresAt() <= gameTime) continue;
            Vec3 center = Vec3.atCenterOf(field.center());
            float radius = field.radius();
            AABB bounds = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );
            if (frustum != null && !frustum.isVisible(bounds)) continue;

            poseStack.pushPose();
            poseStack.translate(center.x, center.y, center.z);
            poseStack.mulPose(Axis.YP.rotation(rotation));
            poseStack.scale(0.65F, 0.65F, 0.65F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    totem,
                    ItemDisplayContext.GROUND,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    level,
                    0
            );
            poseStack.popPose();
        }
        bufferSource.endBatch();

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawSphere(PoseStack poseStack, float radius, int rings, int segments, float alpha) {
        //? if <1.21 {
        /*BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        *///?} else {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        //?}

        Matrix4f pose = poseStack.last().pose();
        int red = (int) (LIGHT_R * 255.0F);
        int green = (int) (LIGHT_G * 255.0F);
        int blue = (int) (LIGHT_B * 255.0F);
        int alphaByte = (int) (Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);

        for (int ring = 0; ring < rings; ring++) {
            double phi0 = -Math.PI / 2.0 + Math.PI * ring / rings;
            double phi1 = -Math.PI / 2.0 + Math.PI * (ring + 1) / rings;
            float y0 = (float) Math.sin(phi0) * radius;
            float y1 = (float) Math.sin(phi1) * radius;
            float r0 = (float) Math.cos(phi0) * radius;
            float r1 = (float) Math.cos(phi1) * radius;

            for (int segment = 0; segment < segments; segment++) {
                double theta0 = 2.0 * Math.PI * segment / segments;
                double theta1 = 2.0 * Math.PI * (segment + 1) / segments;
                addVertex(builder, pose, r0 * (float) Math.cos(theta0), y0,
                        r0 * (float) Math.sin(theta0), red, green, blue, alphaByte);
                addVertex(builder, pose, r1 * (float) Math.cos(theta0), y1,
                        r1 * (float) Math.sin(theta0), red, green, blue, alphaByte);
                addVertex(builder, pose, r1 * (float) Math.cos(theta1), y1,
                        r1 * (float) Math.sin(theta1), red, green, blue, alphaByte);
                addVertex(builder, pose, r0 * (float) Math.cos(theta1), y0,
                        r0 * (float) Math.sin(theta1), red, green, blue, alphaByte);
            }
        }

        //? if <1.21 {
        /*BufferBuilder.RenderedBuffer rendered = builder.end();
        BufferUploader.drawWithShader(rendered);
        *///?} else {
        BufferUploader.drawWithShader(builder.build());
        //?}
    }

    private static void addVertex(BufferBuilder builder, Matrix4f pose, float x, float y, float z,
                                  int red, int green, int blue, int alpha) {
        //? if <1.21 {
        /*builder.vertex(pose, x, y, z).color(red, green, blue, alpha).endVertex();
        *///?} else {
        builder.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
        //?}
    }
}
