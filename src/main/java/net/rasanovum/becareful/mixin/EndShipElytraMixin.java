package net.rasanovum.becareful.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.rasanovum.becareful.BeCarefulConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*? if <1.21 {*/
/*import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
*//*?}*/

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.structures.EndCityPieces$EndCityPiece")
public class EndShipElytraMixin {
    @Redirect(
            method = "handleDataMarker",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setItem(Lnet/minecraft/world/item/ItemStack;Z)V"
            )
    )
    private void replaceEndShipElytra(ItemFrame frame, ItemStack original, boolean updateNeighbours,
                                      String marker, BlockPos pos, ServerLevelAccessor level,
                                      RandomSource random, BoundingBox box) {
        frame.setItem(marker.startsWith("Elytra") && !BeCarefulConfig.spawnElytra
                ? createBindingCurseBook(level)
                : original, updateNeighbours);
    }

    private static ItemStack createBindingCurseBook(ServerLevelAccessor level) {
        /*? if <1.21 {*/
        /*return EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(Enchantments.BINDING_CURSE, 1)
        );
        *//*?} else {*/
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(
                level.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.BINDING_CURSE),
                1
        );
        return book;
        /*?}*/
    }
}
