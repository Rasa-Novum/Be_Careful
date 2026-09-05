package net.rasanovum.becareful.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class LostKeyItem extends Item {
    public LostKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    /*? if <1.21 {*/
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    *//*?} else {*/
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
    /*?}*/
        tooltip.add(Component.translatable("tooltip.be_careful.lost_key").withStyle(ChatFormatting.GRAY));
    }
}
