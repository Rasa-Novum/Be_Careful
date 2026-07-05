package net.rasanovum.becareful.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CampfireBlock;
import net.rasanovum.becareful.blocks.FrozenCampfireBlockEntity;

public class FrozenCampfireRenderer implements BlockEntityRenderer<FrozenCampfireBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FrozenCampfireRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FrozenCampfireBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction direction = blockEntity.getBlockState().getValue(CampfireBlock.FACING);

        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            ItemStack itemStack = blockEntity.getItem(i);
            if (itemStack != ItemStack.EMPTY && !itemStack.isEmpty()) {
                poseStack.pushPose();

                poseStack.translate(0.5D, 0.44D, 0.5D);

                float rotationAngle = -direction.toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

                switch (i) {
                    case 0 -> poseStack.translate(-0.27D, -0.27D, 0.0D); // Top Left Corner
                    case 1 -> poseStack.translate(0.27D, -0.27D, 0.0D);  // Top Right Corner
                    case 2 -> poseStack.translate(0.27D, 0.27D, 0.0D);   // Bottom Right Corner
                    case 3 -> poseStack.translate(-0.27D, 0.27D, 0.0D);  // Bottom Left Corner
                }

                poseStack.scale(0.375F, 0.375F, 0.375F);

                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), i);
                poseStack.popPose();
            }
        }
    }
}