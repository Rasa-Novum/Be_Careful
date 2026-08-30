package net.rasanovum.becareful.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.rasanovum.becareful.light.ClientLightFieldState;
import net.rasanovum.becareful.light.LightField;
import org.joml.Matrix4f;

public final class LightFieldRenderer {
    private static final int SHELL_RINGS = 24;
    private static final int SHELL_SEGMENTS = 48;
    private static final float SHELL_ALPHA = 0.25F;
    private static final float LIGHT_R = 1.0F;
    private static final float LIGHT_G = 0.78F;
    private static final float LIGHT_B = 0.20F;
    private static final float[] UNIT_SPHERE_VERTICES = createUnitSphereVertices();

    private LightFieldRenderer() {}

    public static void render(PoseStack poseStack, ClientLevel level, Player player, float tickDelta, Frustum frustum) {
        if (ClientLightFieldState.get().isEmpty()) return;

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        long gameTime = level.getGameTime();
        //? if <1.21 {
        /*float frameDelta = Minecraft.getInstance().getFrameTime();
        *///?} else {
        float frameDelta = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        //?}
        double renderTime = gameTime + Math.max(0.0, Math.min(1.0, frameDelta));

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        boolean useContactShader = canRenderContactGlow();
        if (!useContactShader) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }

        poseStack.pushPose();

        if (!useContactShader) {
            for (LightField field : ClientLightFieldState.get()) {
                if (field.expiresAt() <= gameTime) continue;

                LightField.FieldState state = field.stateAt(renderTime);
                Vec3 center = field.center();
                float radius = state.radius();
                AABB bounds = new AABB(
                        center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius
                );
                if (frustum != null && !frustum.isVisible(bounds)) continue;

                poseStack.pushPose();
                poseStack.translate(center.x, center.y, center.z);
                drawSphere(poseStack, radius, SHELL_ALPHA * state.opacity());
                poseStack.popPose();
            }
        }

        if (useContactShader) {
            renderNoisyShell(poseStack, camera, renderTime, gameTime, frustum);
            renderContactGlow(poseStack, camera, renderTime, gameTime, frustum);
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderNoisyShell(
            PoseStack poseStack, Vec3 camera, double renderTime, long gameTime, Frustum frustum
    ) {
        ShaderInstance shader = LightFieldShader.get();
        shader.getUniform("RenderMode").set(0.0F);
        shader.getUniform("CameraPosition").set(
                (float) camera.x, (float) camera.y, (float) camera.z
        );
        shader.getUniform("Time").set((float) (renderTime / 20.0));

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        for (LightField field : ClientLightFieldState.get()) {
            if (field.expiresAt() <= gameTime) continue;

            LightField.FieldState state = field.stateAt(renderTime);
            Vec3 center = field.center();
            float radius = state.radius();
            AABB bounds = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );
            if (frustum != null && !frustum.isVisible(bounds)) continue;

            poseStack.pushPose();
            poseStack.translate(center.x, center.y, center.z);
            drawSphere(poseStack, radius, state.opacity());
            poseStack.popPose();
        }
    }

    private static void renderContactGlow(
            PoseStack poseStack, Vec3 camera, double renderTime, long gameTime, Frustum frustum
    ) {
        ShaderInstance shader = LightFieldShader.get();
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();

        Matrix4f inverseViewProjection = new Matrix4f(RenderSystem.getProjectionMatrix())
                .mul(RenderSystem.getModelViewMatrix())
                .invert();
        shader.setSampler("SceneDepth", mainTarget.getDepthTextureId());
        shader.getUniform("InverseViewProjection").set(inverseViewProjection);
        shader.getUniform("ScreenSize").set((float) mainTarget.width, (float) mainTarget.height);
        shader.getUniform("ContactWidth").set(0.08F);
        shader.getUniform("RenderMode").set(1.0F);
        shader.getUniform("CameraPosition").set(
                (float) camera.x, (float) camera.y, (float) camera.z
        );
        shader.getUniform("Time").set((float) (renderTime / 20.0));

        RenderSystem.setShader(() -> shader);
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE
        );

        for (LightField field : ClientLightFieldState.get()) {
            if (field.expiresAt() <= gameTime) continue;

            LightField.FieldState state = field.stateAt(renderTime);
            Vec3 center = field.center();
            float radius = state.radius();
            AABB bounds = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );
            if (frustum != null && !frustum.isVisible(bounds)) continue;

            shader.getUniform("FieldCenter").set(
                    (float) (center.x - camera.x),
                    (float) (center.y - camera.y),
                    (float) (center.z - camera.z)
            );
            shader.getUniform("FieldRadius").set(radius);

            poseStack.pushPose();
            poseStack.translate(center.x, center.y, center.z);
            drawSphere(poseStack, radius, state.opacity());
            poseStack.popPose();
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    private static boolean canRenderContactGlow() {
        return LightFieldShader.get() != null
                && Minecraft.getInstance().getMainRenderTarget().getDepthTextureId() > 0
                && !IrisCompat.isShaderPackInUse();
    }

    private static void drawSphere(PoseStack poseStack, float radius, float alpha) {
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

        for (int i = 0; i < UNIT_SPHERE_VERTICES.length; i += 3) {
            addVertex(
                    builder, pose,
                    UNIT_SPHERE_VERTICES[i] * radius,
                    UNIT_SPHERE_VERTICES[i + 1] * radius,
                    UNIT_SPHERE_VERTICES[i + 2] * radius,
                    red, green, blue, alphaByte
            );
        }

        //? if <1.21 {
        /*BufferBuilder.RenderedBuffer rendered = builder.end();
        BufferUploader.drawWithShader(rendered);
        *///?} else {
        BufferUploader.drawWithShader(builder.build());
        //?}
    }

    private static float[] createUnitSphereVertices() {
        float[] vertices = new float[SHELL_RINGS * SHELL_SEGMENTS * 4 * 3];
        int index = 0;
        for (int ring = 0; ring < SHELL_RINGS; ring++) {
            double phi0 = -Math.PI / 2.0 + Math.PI * ring / SHELL_RINGS;
            double phi1 = -Math.PI / 2.0 + Math.PI * (ring + 1) / SHELL_RINGS;
            float y0 = (float) Math.sin(phi0);
            float y1 = (float) Math.sin(phi1);
            float r0 = (float) Math.cos(phi0);
            float r1 = (float) Math.cos(phi1);

            for (int segment = 0; segment < SHELL_SEGMENTS; segment++) {
                double theta0 = 2.0 * Math.PI * segment / SHELL_SEGMENTS;
                double theta1 = 2.0 * Math.PI * (segment + 1) / SHELL_SEGMENTS;
                index = addUnitVertex(vertices, index, r0 * (float) Math.cos(theta0), y0,
                        r0 * (float) Math.sin(theta0));
                index = addUnitVertex(vertices, index, r1 * (float) Math.cos(theta0), y1,
                        r1 * (float) Math.sin(theta0));
                index = addUnitVertex(vertices, index, r1 * (float) Math.cos(theta1), y1,
                        r1 * (float) Math.sin(theta1));
                index = addUnitVertex(vertices, index, r0 * (float) Math.cos(theta1), y0,
                        r0 * (float) Math.sin(theta1));
            }
        }
        return vertices;
    }

    private static int addUnitVertex(float[] vertices, int index, float x, float y, float z) {
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z;
        return index;
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
