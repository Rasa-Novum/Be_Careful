package net.rasanovum.becareful.effects;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import static net.rasanovum.becareful.BeCareful.MOD_ID;
import net.rasanovum.rosetta.util.RegistryCompat;

public class CorruptionEffect extends MobEffect {
    public CorruptionEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B0082);
    }
    public static final ResourceKey<DamageType> CORRUPTION_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, RegistryCompat.getLocation(MOD_ID, "corruption"));

    /*? if <1.21 {*/
    /*@Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        int k = 20 >> pAmplifier;
        if (k > 0) {
            return pDuration % k == 0;
        } else {
            return true;
        }
    }
    *//*?}*/

    /*? if <1.21 {*/
    /*@Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
    *//*?} else {*/
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
    /*?}*/
        Level level = entity.level();
        if (!level.isClientSide()) {
            entity.hurt(level.damageSources().source(CORRUPTION_DAMAGE), 2.0f);

        } else {
            entity.hurt(entity.damageSources().magic(), 1.0f + amplifier);
        }

        entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
        /*? if >=1.21 {*/
        return true;
        /*?}*/
    }

    /*? if <1.21 {*/
    /*@Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (entity instanceof ServerPlayer player) {
            player.playSound(SoundEvents.PLAYER_BREATH, 1.0f, 1.0f);
        }
    }
    *//*?}*/
}

