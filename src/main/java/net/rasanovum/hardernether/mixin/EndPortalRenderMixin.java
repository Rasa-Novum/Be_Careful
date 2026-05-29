package net.rasanovum.hardernether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TheEndPortalRenderer.class)
public class EndPortalRenderMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/TheEndPortalBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD")
    )
    private void rotateRenderingPlaneToVertical(TheEndPortalBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        if (level != null && level.getBiome(pos).is(Biomes.DEEP_DARK)) {
            poseStack.translate(0.5D, 0.5D, 0.5D);  // shift pivot point to centre for proper rotation

            boolean runsAlongX = level.getBlockState(pos.east()).is(Blocks.END_PORTAL) ||
                    level.getBlockState(pos.west()).is(Blocks.END_PORTAL) ||
                    level.getBlockState(pos.east()).is(Blocks.REINFORCED_DEEPSLATE) ||
                    level.getBlockState(pos.west()).is(Blocks.REINFORCED_DEEPSLATE);

            if (runsAlongX){
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            } else {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.F));
            }
            poseStack.translate(-0.5D, -0.5D, -0.5D); //shift rendering coordinates back
        }
    }
}
