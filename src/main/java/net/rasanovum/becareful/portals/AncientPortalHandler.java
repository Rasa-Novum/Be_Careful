package net.rasanovum.becareful.portals;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;

public class AncientPortalHandler {

    public static final boolean DEEP_DARK_ENABLED = BeCarefulConfig.doDeepDarkFeatures;

    public static void registerEvents() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, killedEntity) -> {
            if (killedEntity instanceof Warden && !world.isClientSide() && DEEP_DARK_ENABLED) {
                ItemEntity keyDrop = new ItemEntity(
                        world,
                        killedEntity.getX(), killedEntity.getY(), killedEntity.getZ(),
                        new ItemStack(BeCareful.LOST_KEY)
                );
                world.addFreshEntity(keyDrop);
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack heldItem = player.getItemInHand(hand);
            BlockPos clickedPos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(clickedPos);

            if (heldItem.is(BeCareful.LOST_KEY) && state.is(Blocks.REINFORCED_DEEPSLATE) && DEEP_DARK_ENABLED) {

                if (!level.isClientSide()) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    ignitePortalGateway(serverLevel, clickedPos);
                    level.playSound(
                            null, clickedPos,
                            SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS,
                            1.0F, 1.0F
                    );

                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                }

                player.swing(hand);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
    private static void ignitePortalGateway(ServerLevel level, BlockPos framePos) {
        BlockPos innerStart = findInnerPortalAir(level, framePos);
        if (innerStart == null && !DEEP_DARK_ENABLED) return;

        int minX = innerStart.getX(), maxX = innerStart.getX();
        int minY = innerStart.getY(), maxY = innerStart.getY();
        int minZ = innerStart.getZ(), maxZ = innerStart.getZ();

        boolean isAxisX = level.getBlockState(framePos.east()).is(Blocks.REINFORCED_DEEPSLATE) ||
                level.getBlockState(framePos.west()).is(Blocks.REINFORCED_DEEPSLATE);

        while (!level.getBlockState(new BlockPos(innerStart.getX(), minY - 1, innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE) && minY > level.getMinBuildHeight()) { minY--; }
        while (!level.getBlockState(new BlockPos(innerStart.getX(), maxY + 1, innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE) && maxY < level.getMaxBuildHeight()) { maxY++; }

        if (isAxisX) {
            while (!level.getBlockState(new BlockPos(minX - 1, innerStart.getY(), innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE)) { minX--; }
            while (!level.getBlockState(new BlockPos(maxX + 1, innerStart.getY(), innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE)) { maxX++; }
        } else {
            while (!level.getBlockState(new BlockPos(innerStart.getX(), innerStart.getY(), minZ - 1)).is(Blocks.REINFORCED_DEEPSLATE)) { minZ--; }
            while (!level.getBlockState(new BlockPos(innerStart.getX(), innerStart.getY(), maxZ + 1)).is(Blocks.REINFORCED_DEEPSLATE)) { maxZ++; }
        }

        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    targetPos.set(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.isAir() ||
                            targetState.is(Blocks.SCULK_VEIN) ||
                            targetState.is(Blocks.SCULK) ||
                            targetState.is(Blocks.WATER)) {

                        level.setBlock(targetPos, Blocks.END_PORTAL.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static BlockPos findInnerPortalAir(Level level, BlockPos clicked) {
        for (Direction dir : Direction.values()) {
            BlockPos offset = clicked.relative(dir);
            BlockState state = level.getBlockState(offset);
            if (state.isAir() || state.is(Blocks.SCULK_VEIN)) {
                return offset;
            }
        }
        return null;
    }
}
