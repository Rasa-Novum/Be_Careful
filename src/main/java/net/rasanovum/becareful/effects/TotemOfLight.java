package net.rasanovum.becareful.effects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.rasanovum.becareful.BeCareful;

import java.util.List;

public class TotemOfLight extends Item {
    public TotemOfLight(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 30;
    }

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

            double radius = 10.0;
            AABB area = player.getBoundingBox().inflate(radius);
            List<Player> playersInRange = pLevel.getEntitiesOfClass(Player.class, area);

            for (Player target : playersInRange) {
                target.removeEffect(BeCareful.CORRUPTION);
                BeCareful.DEEP_DARK_TIMERS.put(target.getUUID(), 0);

                target.displayClientMessage(
                        Component.literal("The light cleanses the smothering darkness...")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
            }

            pLevel.broadcastEntityEvent(player, (byte) 35);

            if (!player.getAbilities().instabuild) {
                pStack.shrink(1);
            }
        }
        return pStack;
    }
}