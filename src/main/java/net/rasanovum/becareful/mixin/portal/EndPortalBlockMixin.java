package net.rasanovum.becareful.mixin.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.rasanovum.becareful.portals.AncientPortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    private static final VoxelShape X_SHAPE = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    private static final VoxelShape Z_SHAPE = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void useVerticalPortalShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        Direction.Axis axis = AncientPortalShape.getAxis(level, pos);
        if (axis == Direction.Axis.X) {
            cir.setReturnValue(X_SHAPE);
        } else if (axis == Direction.Axis.Z) {
            cir.setReturnValue(Z_SHAPE);
        }
    }
}
