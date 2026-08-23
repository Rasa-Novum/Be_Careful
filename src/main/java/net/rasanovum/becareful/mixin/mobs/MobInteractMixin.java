package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.warden.WardenStunAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobInteractMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void beCareful$claimStunnedWarden(Player player, InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResult> cir) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Warden warden)
                || !((WardenStunAccess) warden).beCareful$isStunned()
                || !player.getItemInHand(hand).isEmpty()) {
            return;
        }

        if (!mob.level().isClientSide()) {
            ItemStack key = new ItemStack(BeCareful.LOST_KEY);
            if (!player.addItem(key)) {
                player.drop(key, false);
            }
            warden.level().playSound(
                    null, warden.getX(), warden.getY(), warden.getZ(),
                    SoundEvents.WARDEN_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F
            );
            warden.setHealth(0.0F);
            warden.die(player.damageSources().playerAttack(player));
        }

        cir.setReturnValue(InteractionResult.sidedSuccess(mob.level().isClientSide()));
    }
}
