package net.rasanovum.becareful.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rasanovum.becareful.BeCarefulConfig;

public class EndEdgeGatewayManager {

    private static final int TARGET_GATEWAY_COUNT = BeCarefulConfig.maxEndGateways;
    private static int generatedCount = 0;

    public static void checkAndSpawnEdgeGateways(ServerLevel level) {
        if (generatedCount >= TARGET_GATEWAY_COUNT) return;

        if (level.getRandom().nextFloat() < 0.005F) {

            double angle = level.getRandom().nextDouble() * 2.0 * Math.PI;
            BlockPos edgePos = null;

            for (int radius = 900; radius < 1100; radius++) {
                int x = (int) (Math.cos(angle) * radius);
                int z = (int) (Math.sin(angle) * radius);

                BlockPos checkPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
                if (level.getBlockState(checkPos.below()).is(Blocks.END_STONE)) {
                    edgePos = checkPos;
                    break;
                }
            }

            if (edgePos != null) {
                BlockPos gatewayPos = edgePos.above(2);

                EndGatewayGenerator.spawnReturnGateway(level, gatewayPos);
                generatedCount++;
            }
        }
    }
}