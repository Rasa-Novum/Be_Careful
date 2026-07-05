package net.rasanovum.becareful.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FoodEatMixin {
    @Inject(method = "eat", at = @At("TAIL"))
    private void onEat(Level pLevel, ItemStack pStack, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!pLevel.isClientSide() && entity instanceof ServerPlayer player && BeCarefulConfig.doNetherFeatures) {
            if (pStack.is(BeCareful.FIRE_RESISTANT_FOODS)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
                player.displayClientMessage(Component.literal("The food cools your core...") // need to replace with key
                        .withStyle(ChatFormatting.AQUA), true);
            }
        }
    }
}
