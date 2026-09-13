package net.rasanovum.becareful.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.rasanovum.becareful.effects.ChampionOfTheDarkEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class ChampionEffectBackgroundMixin extends Screen {
    @Unique
    private int beCareful$effectWidth = 120;

    protected ChampionEffectBackgroundMixin(Component title) {
        super(title);
    }

    @Shadow
    private Component getEffectName(MobEffectInstance effect) {
        throw new AssertionError();
    }

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void beCareful$measureChampion(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        beCareful$effectWidth = 120;
        if (minecraft == null || minecraft.player == null) return;
        for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
            if (ChampionOfTheDarkEffect.isChampion(effect)) {
                beCareful$effectWidth = Math.max(beCareful$effectWidth, 28 + font.width(getEffectName(effect)) + 8);
            }
        }
    }

    @ModifyConstant(method = "renderEffects", constant = @Constant(intValue = 120))
    private int beCareful$requireRoomForChampion(int original) {
        return beCareful$effectWidth;
    }

    /*? if >=1.21 {*/
    @ModifyArg(method = "renderBackgrounds", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"), index = 3)
    private int beCareful$expandBackground(int width) {
        return width == 120 ? beCareful$effectWidth : width;
    }
    /*?} else {*/
    /*@Redirect(method = "renderBackgrounds", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void beCareful$expandBackground(GuiGraphics graphics, ResourceLocation texture,
                                           int x, int y, int u, int v, int width, int height) {
        if (width == 120 && beCareful$effectWidth > width) {
            graphics.blitNineSliced(texture, x, y, beCareful$effectWidth, height, 4, width, height, u, v);
        } else {
            graphics.blit(texture, x, y, u, v, width, height);
        }
    }
    *//*?}*/
}

