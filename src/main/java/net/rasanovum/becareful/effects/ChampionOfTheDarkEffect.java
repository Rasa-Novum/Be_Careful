package net.rasanovum.becareful.effects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.rasanovum.becareful.BeCarefulContent;

public final class ChampionOfTheDarkEffect extends MobEffect {
    public ChampionOfTheDarkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x43566D);
    }

    public static boolean protects(LivingEntity entity) {
        /*? if <1.21 {*/
        /*return entity.hasEffect(BeCarefulContent.CHAMPION_OF_THE_DARK.get());
        *//*?} else {*/
        return entity.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(BeCarefulContent.CHAMPION_OF_THE_DARK.get()));
        /*?}*/
    }

    public static boolean isChampion(MobEffectInstance instance) {
        /*? if <1.21 {*/
        /*return instance.getEffect() == BeCarefulContent.CHAMPION_OF_THE_DARK.get();
        *//*?} else {*/
        return instance.getEffect().value() == BeCarefulContent.CHAMPION_OF_THE_DARK.get();
        /*?}*/
    }

    public static void grant(LivingEntity entity, int duration) {
        /*? if <1.21 {*/
        /*entity.addEffect(new MobEffectInstance(BeCarefulContent.CHAMPION_OF_THE_DARK.get(), duration, 0, false, false, true));
        *//*?} else {*/
        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(
                BeCarefulContent.CHAMPION_OF_THE_DARK.get()), duration, 0, false, false, true));
        /*?}*/
    }

    /*? if neoforge {*/
    /*@Override
    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, MobEffectInstance instance) {
        cures.addAll(net.neoforged.neoforge.common.EffectCures.DEFAULT_CURES);
        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);
    }
    *//*?}*/
}
