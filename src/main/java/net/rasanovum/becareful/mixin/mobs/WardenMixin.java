package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.util.AdvancementManager;
import net.rasanovum.becareful.warden.WardenStunAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public class WardenMixin implements WardenStunAccess {
    @Unique
    private static final int BE_CAREFUL$STUN_DURATION_TICKS = 60;

    @Unique
    private static final EntityDataAccessor<Boolean> BE_CAREFUL$STUNNED =
            SynchedEntityData.defineId(Warden.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private int beCareful$stunTicksRemaining;

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void reduceWardenBaseHealth(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        int WardenHealthValue = BeCarefulConfig.wardenHealthValue;

        AttributeSupplier.Builder builder = cir.getReturnValue();

        builder.add(Attributes.MAX_HEALTH, WardenHealthValue);

        cir.setReturnValue(builder);
    }

    /*? if <1.21 {*/
    /*@Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void beCareful$defineStunnedData(CallbackInfo ci) {
        ((Warden) (Object) this).getEntityData().define(BE_CAREFUL$STUNNED, false);
    }
    *//*?} else {*/
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void beCareful$defineStunnedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BE_CAREFUL$STUNNED, false);
    }
    /*?}*/

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void beCareful$saveStunned(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("BeCarefulStunned", beCareful$isStunned());
        if (beCareful$isStunned()) {
            tag.putInt("BeCarefulStunTicks", beCareful$stunTicksRemaining);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void beCareful$loadStunned(CompoundTag tag, CallbackInfo ci) {
        if (tag.getBoolean("BeCarefulStunned")) {
            beCareful$beginStun(false);
            beCareful$stunTicksRemaining = Math.max(1, tag.getInt("BeCarefulStunTicks"));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void beCareful$tickStun(CallbackInfo ci) {
        Warden warden = (Warden) (Object) this;
        if (!warden.level().isClientSide() && beCareful$isStunned()
                && --beCareful$stunTicksRemaining <= 0) {
            beCareful$resume();
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void beCareful$blockDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!BeCarefulConfig.wardenInvulnerable
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof Player player && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AdvancementManager.award(serverPlayer, AdvancementManager.ATTACK_WARDEN);
            Warden warden = (Warden) (Object) this;
            warden.level().playSound(
                    null, warden.getX(), warden.getY(), warden.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 0.8F + warden.getRandom().nextFloat() * 0.4F
            );
        }
        cir.setReturnValue(false);
    }

    @Override
    public boolean beCareful$isStunned() {
        return ((Warden) (Object) this).getEntityData().get(BE_CAREFUL$STUNNED);
    }

    @Override
    public void beCareful$stun() {
        if (beCareful$isStunned()) {
            return;
        }

        beCareful$beginStun(true);
    }

    @Unique
    private void beCareful$beginStun(boolean playSound) {
        Warden warden = (Warden) (Object) this;

        warden.getEntityData().set(BE_CAREFUL$STUNNED, true);
        beCareful$stunTicksRemaining = BE_CAREFUL$STUN_DURATION_TICKS;
        warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
        warden.getBrain().eraseMemory(MemoryModuleType.SONIC_BOOM_SOUND_DELAY);
        warden.getBrain().eraseMemory(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN);
        warden.setTarget(null);
        warden.setNoAi(true);
        warden.setPose(Pose.ROARING);
        if (playSound) {
            warden.level().playSound(
                    null, warden.getX(), warden.getY(), warden.getZ(),
                    SoundEvents.WARDEN_HURT, SoundSource.HOSTILE, 1.0F, 1.0F
            );
        }
    }

    @Unique
    private void beCareful$resume() {
        Warden warden = (Warden) (Object) this;
        beCareful$stunTicksRemaining = 0;
        warden.getEntityData().set(BE_CAREFUL$STUNNED, false);
        warden.setNoAi(false);
        warden.setPose(Pose.STANDING);
        warden.level().playSound(
                null, warden.getX(), warden.getY(), warden.getZ(),
                SoundEvents.WARDEN_ANGRY, SoundSource.HOSTILE, 1.0F, 1.0F
        );
    }
}
