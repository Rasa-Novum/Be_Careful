package net.rasanovum.becareful.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.rasanovum.becareful.client.ClientTotemUse;
import net.rasanovum.becareful.effects.TotemOfLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class TotemUseMixin {
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void beCareful$requireRelease(Player player, InteractionHand hand,
                                         CallbackInfoReturnable<InteractionResult> cir) {
        if (player.getItemInHand(hand).getItem() instanceof TotemOfLight && ClientTotemUse.isAwaitingRelease()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void beCareful$trackTotemUse(Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction() && player.isUsingItem()
                && player.getUseItem().getItem() instanceof TotemOfLight) {
            ClientTotemUse.started();
        }
    }
}
