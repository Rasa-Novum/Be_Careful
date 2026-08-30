package net.rasanovum.becareful.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.rasanovum.becareful.BeCarefulConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ShelterEvaluator {
    private ShelterEvaluator() {
    }

    public static ShelterStatus evaluate(ServerLevel level, BlockPos anchor) {
        BlockPos start = anchor.above().immutable();
        int maxBlocks = Math.max(1, BeCarefulConfig.shelterMaxBlocks);
        int maxDistance = Math.max(1, BeCarefulConfig.shelterMaxDistance);
        int maxDistanceSquared = maxDistance * maxDistance;

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> volume = new ArrayList<>();
        visited.add(start);
        queue.add(start);

        int minimumBlockLight = 15;
        int maximumSkyLight = 0;
        boolean processed = false;
        boolean bounded = true;

        if (!isPassable(level, start)) {
            return new ShelterStatus(anchor, 0, 15, false, false, false, false, List.of());
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            volume.add(current);

            minimumBlockLight = Math.min(minimumBlockLight, level.getBrightness(LightLayer.BLOCK, current));
            maximumSkyLight = Math.max(maximumSkyLight, level.getBrightness(LightLayer.SKY, current));

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                BlockState neighborState = level.getBlockState(neighbor);

                if (ChunkTameManager.isProcessedSettlementBlock(neighborState)) {
                    processed = true;
                }

                if (!isPassable(level, neighbor) || visited.contains(neighbor)) {
                    continue;
                }

                int dx = neighbor.getX() - start.getX();
                int dy = neighbor.getY() - start.getY();
                int dz = neighbor.getZ() - start.getZ();
                if (dx * dx + dy * dy + dz * dz > maxDistanceSquared || visited.size() >= maxBlocks) {
                    bounded = false;
                    continue;
                }

                visited.add(neighbor.immutable());
                queue.add(neighbor);
            }
        }

        int requiredLight = Math.max(0, Math.min(15, BeCarefulConfig.shelterLightLevel));
        return new ShelterStatus(
                anchor,
                minimumBlockLight,
                maximumSkyLight,
                minimumBlockLight >= requiredLight,
                maximumSkyLight < 15,
                processed,
                bounded,
                volume
        );
    }

    private static boolean isPassable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty()
                && state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty();
    }
}
