package net.rasanovum.hardernether.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class OnFireEntityMixin {
    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void hideFireOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        Player player = minecraft.player;
        if (player != null) {
            if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                ci.cancel();
            }
        }
    }
}
