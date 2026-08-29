package net.rasanovum.becareful.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
//? if <1.21 {
//?} else {
import net.minecraft.client.DeltaTracker;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class WarningOverlayMixin {
    @Unique private static final float MAX_WARNING_JIGGLE_PIXELS = 3.0F;

    @Shadow private Component overlayMessageString;

    @Unique private boolean beCareful$jiggleApplied;

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"))
    private void beCareful$beginWarningJiggle(
            GuiGraphics graphics,
            //? if <1.21 {
            /*float legacyPartialTick,
            *///?} else {
            DeltaTracker deltaTracker,
            //?}
            CallbackInfo ci
    ) {
        if (!beCareful$isDeepDarkWarning(this.overlayMessageString)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        //? if <1.21 {
        /*float partialTick = legacyPartialTick;
        *///?} else {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        //?}
        float progress = Mth.clamp(ClientCorruptionState.get(), 0.0F, 1.0F);
        float amplitude = progress * MAX_WARNING_JIGGLE_PIXELS;
        float animationTime = minecraft.level.getGameTime() + partialTick;
        float offsetX = Mth.sin(animationTime * 0.91F) * amplitude;
        float offsetY = Mth.cos(animationTime * 1.17F) * amplitude;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(offsetX, offsetY, 0.0F);
        beCareful$jiggleApplied = true;
    }

    @Inject(method = "renderOverlayMessage", at = @At("RETURN"))
    private void beCareful$endWarningJiggle(
            GuiGraphics graphics,
            //? if <1.21 {
            /*float legacyPartialTick,
            *///?} else {
            DeltaTracker deltaTracker,
            //?}
            CallbackInfo ci
    ) {
        if (beCareful$jiggleApplied) {
            graphics.pose().popPose();
            beCareful$jiggleApplied = false;
        }
    }

    @Unique
    private static boolean beCareful$isDeepDarkWarning(Component component) {
        if (component == null || !(component.getContents() instanceof TranslatableContents contents)) {
            return false;
        }
        return contents.getKey().startsWith("message.be_careful.deep_dark_warning.");
    }
}
