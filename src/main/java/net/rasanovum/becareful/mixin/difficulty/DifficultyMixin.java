package net.rasanovum.becareful.mixin.difficulty;

import net.minecraft.world.DifficultyInstance;
import net.rasanovum.becareful.BeCarefulConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DifficultyInstance.class)
public class DifficultyMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static long invertInhabitedTime(long inhabitedTime) {
        long maxInhabitedTime = 3600000L;

        if (inhabitedTime < maxInhabitedTime && BeCarefulConfig.doDifficultyFeatures) {
            return maxInhabitedTime - inhabitedTime;
        }

        return 0L;
    }
}
