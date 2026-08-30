package net.rasanovum.becareful.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.client.LightFieldRenderer;
import net.rasanovum.becareful.client.ShelterRenderer;
import net.rasanovum.becareful.warden.WardenStunAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "shouldShowEntityOutlines", at = @At("RETURN"), cancellable = true)
    private void beCareful$enableKeyOutlineTarget(CallbackInfoReturnable<Boolean> cir) {
        if (hasWarden()) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "renderLevel", at = @At(value = "LOAD", opcode = Opcodes.ILOAD), index = 23)
    private boolean beCareful$enableKeyOutlinePass(boolean outlinePass) {
        return hasWarden() || outlinePass;
    }

    @Inject(method = "renderDebug", at = @At("TAIL"))
    private void beCareful$renderWorld(PoseStack poseStack, MultiBufferSource bufferSource,
                                       Camera camera, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        poseStack.pushPose();
        var cameraPosition = camera.getPosition();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        LightFieldRenderer.render(poseStack, minecraft.level, minecraft.player, 0.0F, null);
        ShelterRenderer.render(poseStack, minecraft.level, minecraft.player, null);
        poseStack.popPose();
    }

    private static boolean hasWarden() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof Warden warden
                    && ((WardenStunAccess) warden).beCareful$isStunned()) {
                return true;
            }
        }
        return false;
    }
}
