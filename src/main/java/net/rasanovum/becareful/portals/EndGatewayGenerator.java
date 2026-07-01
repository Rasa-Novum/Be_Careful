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
        level.setBlock(corePos.above(), Blocks.OBSIDIAN.defaultBlockState(), 3);
        level.setBlock(corePos.below(), Blocks.OBSIDIAN.defaultBlockState(), 3);
    }
}
