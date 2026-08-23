package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SonicBoom.class)
public abstract class SonicBoomMixin {
    private static final int BE_CAREFUL$CHARGE_DELAY_TICKS = 50;

    @Inject(method = "start", at = @At("TAIL"))
    private void beCareful$extendCharge(ServerLevel level, Warden warden, long gameTime, CallbackInfo ci) {
        warden.getBrain().setMemoryWithExpiry(
                MemoryModuleType.SONIC_BOOM_SOUND_DELAY,
                Unit.INSTANCE,
                BE_CAREFUL$CHARGE_DELAY_TICKS
        );
    }
}
