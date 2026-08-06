package net.rasanovum.becareful.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;

public final class AncientPortalShape {
    private AncientPortalShape() {
    }

    public static Direction.Axis getAxis(BlockGetter level, BlockPos pos) {
        if (!level.getBlockState(pos.above()).is(Blocks.END_PORTAL)
                && !level.getBlockState(pos.below()).is(Blocks.END_PORTAL)) {
            return null;
        }

        int xConnections = connectionCount(level, pos.east(), pos.west());
        int zConnections = connectionCount(level, pos.north(), pos.south());
        if (xConnections == zConnections) return null;
        return xConnections > zConnections ? Direction.Axis.X : Direction.Axis.Z;
    }

    private static int connectionCount(BlockGetter level, BlockPos first, BlockPos second) {
        int count = 0;
        if (isPortalOrFrame(level, first)) count++;
        if (isPortalOrFrame(level, second)) count++;
        return count;
    }

    private static boolean isPortalOrFrame(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.END_PORTAL)
                || level.getBlockState(pos).is(Blocks.REINFORCED_DEEPSLATE);
    }
}
