package net.rasanovum.hardernether;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class TotemOfLight extends Item {
    public TotemOfLight(Settings settings) {
        super(settings);
    }


    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            int revealTick = player.getServer().getTicks() + 40;
            // 10 block radius
            Box cleanseArea = player.getBoundingBox().expand(10.0);
            // find all players in that area
            List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                    ServerPlayerEntity.class,
                    cleanseArea,
                    p -> true
            );
            // cleanse all players in area
            for (ServerPlayerEntity target : nearbyPlayers) {

                world.sendEntityStatus(target, EntityStatuses.USE_TOTEM_OF_UNDYING);
                HarderNether.deepDarkTimers.put(target.getUuid(), 0);
                target.removeStatusEffect(HarderNether.CORRUPTION);
                target.removeStatusEffect(StatusEffects.DARKNESS);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,80,1));

                HarderNether.messageSchedule.put(target.getUuid(), revealTick);
            }

            stack.decrement(1);
        }
        return stack;
    }
}