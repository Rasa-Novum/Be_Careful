package net.rasanovum.becareful.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import net.rasanovum.becareful.BeCarefulConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class DimensionScaleMixin {

    @Inject(method = "coordinateScale", at = @At("HEAD"), cancellable = true)
    private void forceOneToOneScale(CallbackInfoReturnable<Double> cir) {
        DimensionType type = (DimensionType) (Object) this;
        if (type.effectsLocation().getPath().equals("the_nether") && BeCarefulConfig.doNetherFeatures) {
            cir.setReturnValue(1.0);
        }
    }
}
