package net.rasanovum.becareful.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.warden.WardenStunAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "shouldShowEntityOutlines", at = @At("RETURN"), cancellable = true)
    private void beCareful$enableKeyOutlineTarget(CallbackInfoReturnable<Boolean> cir) {
        if (hasWarden()) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "renderLevel", at = @At(value = "LOAD", opcode = Opcodes.ILOAD), index = 23)
    private boolean beCareful$enableKeyOutlinePass(boolean outlinePass) {
        return hasWarden() || outlinePass;
    }

    private static boolean hasWarden() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof Warden warden
                    && ((WardenStunAccess) warden).beCareful$isStunned()) {
                return true;
            }
        }
        return false;
    }
}
