package net.rasanovum.becareful.util;

/*? if fabric {*/
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
/*?}*/
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.blocks.FrozenCampfireBlock;

import java.util.HashMap;
import java.util.UUID;

public class ColdEnvironmentManager {

    private static final int WARNING_TICKS = BeCarefulConfig.frozenWarningTicks;
    private static final int DANGER_TICKS = BeCarefulConfig.frozenDangerTicks;
    private static final int HEAT_CHECK_RADIUS = BeCarefulConfig.heatCheckRadius;

    private static final HashMap<UUID, Integer> playerColdTimers = new HashMap<>();

    /*? if fabric {*/
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(ColdEnvironmentManager::tick);
    }
    /*?}*/

    public static void tick(ServerLevel level) {
            if (level.getGameTime() % 20 == 0) {
                FrozenCampfireBlock.PLAYERS_NEAR_COLD_FIRE.clear();
            }

            for (ServerPlayer player : level.players()) {
                if (player.isCreative() || player.isSpectator()) continue;

                UUID uuid = player.getUUID();
                BlockPos playerPos = player.blockPosition();
                int currentTime = playerColdTimers.getOrDefault(uuid, 0);

                if (isHoldingTorch(player) || isNearHeatSource(level, playerPos) || isNearTorchHeat(level, playerPos)) {
                    if (currentTime > 0) {
                        playerColdTimers.put(uuid, Math.max(0, currentTime - 10));
                        player.setTicksFrozen(Math.max(0, player.getTicksFrozen() - 10));
                    }
                    continue;
                }

                if (level.getBiome(playerPos).value().coldEnoughToSnow(playerPos)) {
                    currentTime++;
                    playerColdTimers.put(uuid, currentTime);

                    if (currentTime >= WARNING_TICKS && currentTime < DANGER_TICKS) {
                        if (player.getTicksFrozen() < 140) {
                            player.setTicksFrozen(player.getTicksFrozen() + 1);
                        }
                    }

                    if (currentTime >= DANGER_TICKS) {
                        player.setTicksFrozen(150);
                        if (level.getGameTime() % 20 == 0) {
                            player.hurt(level.damageSources().freeze(), 2.0F);
                        }
                    }

                } else {
                    if (currentTime > 0) {
                        playerColdTimers.put(uuid, Math.max(0, currentTime - 2));
                        player.setTicksFrozen(Math.max(0, player.getTicksFrozen() - 2));
                    }
                }
            }
    }

    private static boolean isHoldingTorch(ServerPlayer player) {
        boolean torchInHand = false;
        if (player.getMainHandItem().is(Items.TORCH)) {
            torchInHand = true;
        } else if (player.getOffhandItem().is(Items.TORCH)){
            torchInHand = true;
        }
        return torchInHand;
    }

    private static boolean isNearHeatSource(ServerLevel level, BlockPos pos) {
        for (BlockPos targetPos : BlockPos.betweenClosed(
                pos.offset(-HEAT_CHECK_RADIUS, -2, -HEAT_CHECK_RADIUS),
                pos.offset(HEAT_CHECK_RADIUS, 2, HEAT_CHECK_RADIUS))) {

            BlockState state = level.getBlockState(targetPos);
            if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                if (state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) return true;
            }
            if (state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)) {
                if (state.getValue(FurnaceBlock.LIT)) return true;
            }
        }
        return false;
    }

    public static boolean isNearTorchHeat(ServerLevel level, BlockPos pos) {
        int torchRadius = Math.max(1, BeCarefulConfig.frozenCampfireRadius / 4);

        for (BlockPos targetPos : BlockPos.betweenClosed(
                pos.offset(-torchRadius, -1, -torchRadius),
                pos.offset(torchRadius, 2, torchRadius))) {

            BlockState state = level.getBlockState(targetPos);
            if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) {
                return true;
            }
        }

        return false;
    }
}
