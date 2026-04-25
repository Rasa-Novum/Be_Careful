package net.rasanovum.hardernether;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

public class CorruptionEffect extends StatusEffect {
    public CorruptionEffect() {
        super(StatusEffectCategory.HARMFUL, 0x4B0082);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        if (interval > 0) {
            return duration % interval == 0;
        }
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        var registry = entity.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        var entry = registry.getEntry(HarderNether.CORRUPTION_DAMAGE_TYPE);

        if (entry.isPresent()) {
            DamageSource corruptionSource = new DamageSource(entry.get());
            entity.damage(corruptionSource, 2.0f * (amplifier + 1));
        } else {

            entity.damage(entity.getDamageSources().magic(), 1.0f * (amplifier + 1));
        }

        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
        }
    }
}

