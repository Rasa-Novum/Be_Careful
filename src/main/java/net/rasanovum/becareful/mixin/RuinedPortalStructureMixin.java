package net.rasanovum.becareful.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RuinedPortalStructure.class)
public class RuinedPortalStructureMixin {
    @Inject(method = "findGenerationPoint", at = @At("RETURN"), cancellable = true)
    private void rejectSurfaceWaterPortals(
            Structure.GenerationContext context,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir
    ) {
        Optional<Structure.GenerationStub> result = cir.getReturnValue();
        if (result.isEmpty()) {
            return;
        }

        BlockPos origin = result.get().position();
        int portalY = origin.getY();
        int[] offsets = {1, 5, 9, 13};

        for (int offsetX : offsets) {
            for (int offsetZ : offsets) {
                NoiseColumn column = context.chunkGenerator().getBaseColumn(
                        origin.getX() + offsetX,
                        origin.getZ() + offsetZ,
                        context.heightAccessor(),
                        context.randomState()
                );
                if (isAtWaterSurface(column, portalY)) {
                    cir.setReturnValue(Optional.empty());
                    return;
                }
            }
        }
    }

    private static boolean isAtWaterSurface(NoiseColumn column, int portalY) {
        for (int y = portalY - 2; y <= portalY + 2; y++) {
            if (column.getBlock(y).getFluidState().is(FluidTags.WATER)
                    && !column.getBlock(y + 1).getFluidState().is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}
