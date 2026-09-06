package net.rasanovum.becareful.mixin.client;

import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.definitions.WardenAnimation;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.warden.WardenStunAccess;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenModel.class)
public abstract class WardenDeathPoseMixin {
    @Shadow
    public abstract ModelPart root();
    @Unique
    private final Vector3f beCareful$animationVector = new Vector3f();

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/warden/Warden;FFFFF)V", at = @At("TAIL"))
    private void beCareful$animateKeyDeath(Warden warden, float swing, float amount, float age, float yaw, float pitch, CallbackInfo ci) {
        WardenStunAccess stun = (WardenStunAccess) warden;
        if (stun.beCareful$keyDeathDuration() <= 0) return;
        double elapsed = warden.level().getGameTime() - stun.beCareful$keyDeathStartedAt()
                + Math.max(0, Math.min(1, age - warden.tickCount));
        if (elapsed < 0 || elapsed >= stun.beCareful$keyDeathDuration()) return;
        root().getAllParts().forEach(ModelPart::resetPose);
        long cycleMillis = Math.max(1, (long) (WardenAnimation.WARDEN_ROAR.lengthInSeconds() * 1000));
        KeyframeAnimations.animate((WardenModel<?>) (Object) this, WardenAnimation.WARDEN_ROAR,
                (long) (elapsed * 50) % cycleMillis, 1.0F, beCareful$animationVector);
    }
}
