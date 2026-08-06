package net.rasanovum.becareful.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.rasanovum.becareful.portals.AncientPortalShape;

public class AncientEndPortalRenderer implements BlockEntityRenderer<TheEndPortalBlockEntity> {
    private final TheEndPortalRenderer<TheEndPortalBlockEntity> vanillaRenderer;

    public AncientEndPortalRenderer(BlockEntityRendererProvider.Context context) {
        vanillaRenderer = new TheEndPortalRenderer<>(context);
    }

    @Override
    public void render(
            TheEndPortalBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        Direction.Axis axis = level == null ? null : AncientPortalShape.getAxis(level, pos);
        if (axis == null) {
            vanillaRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        boolean irisShaders = IrisCompat.isShaderPackInUse();
        VertexConsumer consumer = bufferSource.getBuffer(irisShaders
                ? RenderType.entitySolid(TheEndPortalRenderer.END_PORTAL_LOCATION)
                : RenderType.endPortal());
        PoseStack.Pose pose = poseStack.last();

        if (axis == Direction.Axis.X) {
            quad(consumer, pose, irisShaders, packedLight, packedOverlay,
                    0.0F, 0.0F, 0.5F, 1.0F, 0.0F, 0.5F,
                    1.0F, 1.0F, 0.5F, 0.0F, 1.0F, 0.5F,
                    0.0F, 0.0F, 1.0F);
            quad(consumer, pose, irisShaders, packedLight, packedOverlay,
                    0.0F, 1.0F, 0.5F, 1.0F, 1.0F, 0.5F,
                    1.0F, 0.0F, 0.5F, 0.0F, 0.0F, 0.5F,
                    0.0F, 0.0F, -1.0F);
        } else {
            quad(consumer, pose, irisShaders, packedLight, packedOverlay,
                    0.5F, 0.0F, 0.0F, 0.5F, 1.0F, 0.0F,
                    0.5F, 1.0F, 1.0F, 0.5F, 0.0F, 1.0F,
                    -1.0F, 0.0F, 0.0F);
            quad(consumer, pose, irisShaders, packedLight, packedOverlay,
                    0.5F, 0.0F, 1.0F, 0.5F, 1.0F, 1.0F,
                    0.5F, 1.0F, 0.0F, 0.5F, 0.0F, 0.0F,
                    1.0F, 0.0F, 0.0F);
        }
    }

    private static void quad(
            VertexConsumer consumer, PoseStack.Pose pose, boolean textured, int light, int overlay,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float normalX, float normalY, float normalZ
    ) {
        vertex(consumer, pose, textured, light, overlay, x1, y1, z1, 0.0F, 1.0F, normalX, normalY, normalZ);
        vertex(consumer, pose, textured, light, overlay, x2, y2, z2, 0.0F, 0.0F, normalX, normalY, normalZ);
        vertex(consumer, pose, textured, light, overlay, x3, y3, z3, 1.0F, 0.0F, normalX, normalY, normalZ);
        vertex(consumer, pose, textured, light, overlay, x4, y4, z4, 1.0F, 1.0F, normalX, normalY, normalZ);
    }

    private static void vertex(
            VertexConsumer consumer, PoseStack.Pose pose, boolean textured, int light, int overlay,
            float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ
    ) {
        if (textured) {
            /*? if <1.21 {*/
            /*consumer.vertex(pose.pose(), x, y, z)
                    .color(0.075F, 0.15F, 0.2F, 1.0F)
                    .uv(u, v)
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(pose.normal(), normalX, normalY, normalZ)
                    .endVertex();
            *//*?} else {*/
            consumer.addVertex(pose, x, y, z)
                    .setColor(0.075F, 0.15F, 0.2F, 1.0F)
                    .setUv(u, v)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(pose, normalX, normalY, normalZ);
            /*?}*/
        } else {
            /*? if <1.21 {*/
            /*consumer.vertex(pose.pose(), x, y, z);
            *//*?} else {*/
            consumer.addVertex(pose.pose(), x, y, z);
            /*?}*/
        }
    }

}
