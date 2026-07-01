package net.rasanovum.becareful.spawning;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.portals.EndEdgeGatewayManager;

public class EndPhantomSpawner {

    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (!BeCarefulConfig.doEndFeatures) return;
            if (level.dimension() != Level.END) return;
            EndEdgeGatewayManager.checkAndSpawnEdgeGateways(level);

            if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return;

            long dayTime = level.getDayTime();
            long attemptsPerDay = 24000 / BeCarefulConfig.phantomSpawnAttemptsPerDay;

            if (dayTime % attemptsPerDay != 0) return;

            RandomSource random = level.getRandom();

            for (ServerPlayer player : level.players()) {
                if (player.isSpectator() || player.isCreative()) continue;

                if (random.nextFloat() < BeCarefulConfig.phantomSpawnRateFloat) {
                    BlockPos playerPos = player.blockPosition();

                    int spawnY = playerPos.getY() + 20 + random.nextInt(15);
                    BlockPos spawnPos = new BlockPos(
                            playerPos.getX() + random.nextInt(10) - 5,
                            spawnY,
                            playerPos.getZ() + random.nextInt(10) - 5
                    );

                    BlockState blockState = level.getBlockState(spawnPos);

                    if (blockState.isAir() && level.getFluidState(spawnPos).isEmpty()) {
                        int packSize = 2 + random.nextInt(2); // Spawns a pack of 2-3 phantoms

                        for (int i = 0; i < packSize; ++i) {
                            Phantom phantom = EntityType.PHANTOM.create(level);
                            if (phantom != null) {
                                phantom.moveTo(spawnPos, 0.0F, 0.0F);
                                phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
                                level.addFreshEntity(phantom);
                            }
                        }
                    }
                }
            }
        });
    }
}