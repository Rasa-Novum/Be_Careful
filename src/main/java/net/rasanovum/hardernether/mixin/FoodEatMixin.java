package net.rasanovum.hardernether.mixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.rasanovum.hardernether.HarderNether;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FoodEatMixin {
    @Inject(method = "eatFood", at = @At("TAIL"))
    private void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!world.isClient && entity instanceof ServerPlayerEntity player) {
            if (stack.isIn(HarderNether.FIRE_RESISTANT_FOODS)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 300, 0));
                player.sendMessage(Text.literal("The food cools your core...").formatted(net.minecraft.util.Formatting.AQUA), true); //no clue if a message is necessary here since the fire resist effect pops up anyway
            }
        }
    }
}
