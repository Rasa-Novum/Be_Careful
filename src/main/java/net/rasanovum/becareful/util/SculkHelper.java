package net.rasanovum.becareful.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.rosetta.util.RegistryCompat;

public final class SculkHelper {
    public static final TagKey<Block> SCULK = TagKey.create(
            Registries.BLOCK, RegistryCompat.getLocation(BeCareful.MOD_ID, "sculk")
    );

    private SculkHelper() {}

    public static boolean hasNearbySculk(ServerLevel level, Vec3 center) {
        double radius = Math.max(1, BeCarefulConfig.lightFieldRadius) * 0.6D;
        double radiusSqr = radius * radius;
        int minY = Math.max(level.getMinBuildHeight(), Mth.floor(center.y - radius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, Mth.floor(center.y + radius));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(center.x - radius); x <= Mth.floor(center.x + radius); x++) {
            for (int z = Mth.floor(center.z - radius); z <= Mth.floor(center.z + radius); z++) {
                pos.set(x, minY, z);
                if (!level.hasChunkAt(pos)) continue;
                for (int y = minY; y <= maxY; y++) {
                    pos.setY(y);
                    if (distanceToBlockSqr(center, pos) <= radiusSqr && level.getBlockState(pos).is(SCULK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static double distanceToBlockSqr(Vec3 point, BlockPos block) {
        double x = distanceToRange(point.x(), block.getX(), block.getX() + 1.0D);
        double y = distanceToRange(point.y(), block.getY(), block.getY() + 1.0D);
        double z = distanceToRange(point.z(), block.getZ(), block.getZ() + 1.0D);
        return x * x + y * y + z * z;
    }

    private static double distanceToRange(double value, double min, double max) {
        return value < min ? min - value : value > max ? value - max : 0.0D;
    }
    public static BlockState replacementFor(BlockState state) {
        if (state.is(Blocks.SCULK)) {
            return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        }
        if (state.is(Blocks.SCULK_VEIN)) {
            BlockState glowLichen = Blocks.GLOW_LICHEN.defaultBlockState();
            for (Direction direction : Direction.values()) {
                if (MultifaceBlock.hasFace(state, direction)) {
                    glowLichen = glowLichen.setValue(MultifaceBlock.getFaceProperty(direction), true);
                }
            }
            return glowLichen;
        }
        if (state.is(Blocks.SCULK_SENSOR) || state.is(Blocks.CALIBRATED_SCULK_SENSOR) || state.is(Blocks.SCULK_SHRIEKER) || state.is(Blocks.SCULK_CATALYST)) {
            return Blocks.AIR.defaultBlockState();
        }
        return null;
    }

}
