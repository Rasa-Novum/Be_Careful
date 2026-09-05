package net.rasanovum.becareful.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import net.rasanovum.becareful.effects.ChampionOfTheDarkEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketMixin {
    /*? if fabric {*/
    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean beCareful$keepChampion(LivingEntity entity) {
        boolean removed = false;
        for (var effect : java.util.List.copyOf(entity.getActiveEffects())) {
            if (ChampionOfTheDarkEffect.isChampion(effect)) continue;
            removed |= entity.removeEffect(effect.getEffect());
        }
        return removed;
    }
    /*?}*/
}
