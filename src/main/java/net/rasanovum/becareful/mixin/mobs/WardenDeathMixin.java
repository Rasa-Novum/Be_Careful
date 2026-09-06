package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.light.WardenDeathWaveManager;
import net.rasanovum.becareful.warden.WardenStunAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class WardenDeathMixin {
    @Shadow
    protected boolean dead;
    @Unique
    private boolean beCareful$deathWaveStarted;

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void beCareful$holdKeyDeath(CallbackInfo ci) {
        if ((Object) this instanceof Warden warden) {
            WardenStunAccess stun = (WardenStunAccess) warden;
            if (stun.beCareful$keyDeathDuration() <= 0) return;
            if (warden.level().getGameTime() - stun.beCareful$keyDeathStartedAt() >= stun.beCareful$keyDeathDuration()) return;
            warden.deathTime = 0;
            warden.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void beCareful$startDeathWave(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof Warden warden && dead
                && warden.level() instanceof ServerLevel level && BeCarefulConfig.doDeepDarkFeatures
                && !beCareful$deathWaveStarted) {
            beCareful$deathWaveStarted = true;
            WardenDeathWaveManager.create(level, warden.position());
        }
    }
}
