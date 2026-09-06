package net.rasanovum.becareful.mixin.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.rasanovum.becareful.client.ClientTotemUse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public abstract class TotemUseKeyMixin {
    @Inject(method = "setDown", at = @At("HEAD"))
    private void beCareful$releaseTotemUse(boolean pressed, CallbackInfo ci) {
        var options = Minecraft.getInstance().options;
        if (!pressed && options != null && (Object) this == options.keyUse) {
            ClientTotemUse.reset();
        }
    }
}
