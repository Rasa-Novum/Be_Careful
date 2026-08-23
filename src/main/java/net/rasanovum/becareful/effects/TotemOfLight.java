package net.rasanovum.becareful.effects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.rasanovum.becareful.light.LightField;
import net.rasanovum.becareful.light.LightFieldManager;
import net.rasanovum.becareful.light.LightFieldNetworking;
import net.rasanovum.becareful.util.AdvancementManager;

public class TotemOfLight extends Item {
    public TotemOfLight(Properties pProperties) {
        super(pProperties);
    }

    /*? if <1.21 {*/
    /*@Override
    public int getUseDuration(ItemStack pStack) {
        return 10;
    }
    *//*?} else {*/
    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity entity) {
        return 10;
    }
    /*?}*/

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!pLevel.isClientSide() && pLivingEntity instanceof Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementManager.award(serverPlayer, AdvancementManager.EYES_UP_GUARDIAN);
                LightFieldNetworking.playTotemAnimation(serverPlayer);
            }

            LightField field = LightFieldManager.create((ServerLevel) pLevel, player);
            for (ServerPlayer target : LightFieldManager.playersInside((ServerLevel) pLevel, field)) {
                target.displayClientMessage(
                        Component.literal("The light cleanses the smothering darkness...")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
            }

            pLevel.playSound(
                    null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F
            );

            if (!player.getAbilities().instabuild) {
                int nextDamage = pStack.getDamageValue() + 1;
                if (nextDamage >= pStack.getMaxDamage()) {
                    pStack.shrink(1);
                } else {
                    pStack.setDamageValue(nextDamage);
                }
            }
        }
        return pStack;
    }
}
