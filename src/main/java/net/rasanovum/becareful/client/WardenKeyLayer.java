package net.rasanovum.becareful.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.rasanovum.becareful.BeCareful;

public final class WardenKeyLayer extends RenderLayer<Warden, WardenModel<Warden>> {
    private static final ItemStack KEY = new ItemStack(BeCareful.LOST_KEY);

    public WardenKeyLayer(RenderLayerParent<Warden, WardenModel<Warden>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Warden warden, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();

        WardenModel<Warden> model = getParentModel();
        ModelPart bone = model.root().getChild("bone");
        ModelPart body = bone.getChild("body");
        bone.translateAndRotate(poseStack);
        body.translateAndRotate(poseStack);

        poseStack.translate(0.0D, -0.2D, -0.35D); // X: L/R, Y: -Height, Z: Depth
        poseStack.mulPose(Axis.XP.rotationDegrees(22.5F)); // Slope
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.75F, 0.75F, 0.75F);

        ItemRenderer itemRenderer = net.minecraft.client.Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(warden, KEY, ItemDisplayContext.FIXED, false, poseStack, buffer, warden.level(), packedLight, LivingEntityRenderer.getOverlayCoords(warden, 0.0F), warden.getId());
        poseStack.popPose();
    }
}
