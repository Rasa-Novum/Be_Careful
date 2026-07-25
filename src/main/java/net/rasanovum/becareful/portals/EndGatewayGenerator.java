package net.rasanovum.becareful.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.rasanovum.becareful.BeCarefulConfig;

public class EndGatewayGenerator {

    public static void spawnReturnGateway(ServerLevel level, BlockPos pos) {
        if (!BeCarefulConfig.doEndFeatures) return;

        level.setBlock(pos, Blocks.END_GATEWAY.defaultBlockState(), 3);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TheEndGatewayBlockEntity gateway) {

            gateway.setExitPosition(new BlockPos(0, 80, 0), true);

            gateway.setChanged();
        }
        generateGatewayShell(level, pos);
    }

    private static void generateGatewayShell(ServerLevel level, BlockPos corePos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }

                    BlockPos shellPos = corePos.offset(dx, dy, dz);
                    boolean isMiddleLayer = dy == 0;
                    boolean isOuterCenter = Math.abs(dy) == 2 && dx == 0 && dz == 0;
                    boolean isOpenSpace = isMiddleLayer || (Math.abs(dy) == 2 && !isOuterCenter);

                    if (isOpenSpace) {
                        level.setBlock(shellPos, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        level.setBlock(shellPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
