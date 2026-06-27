package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.BeCarefulConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public class WardenMixin {

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void reduceWardenBaseHealth(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        int WardenHealthValue = BeCarefulConfig.wardenHealthValue;

        AttributeSupplier.Builder builder = cir.getReturnValue();

        builder.add(Attributes.MAX_HEALTH, WardenHealthValue);

        cir.setReturnValue(builder);
    }
}
