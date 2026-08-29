package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class WardenLootMixin {
    @Inject(method = "dropFromLootTable", at = @At("HEAD"), cancellable = true)
    private void beCareful$disableWardenLoot(DamageSource source, boolean recentlyHit, CallbackInfo ci) {
        if ((Object) this instanceof Warden) {
            ci.cancel();
        }
    }
}
