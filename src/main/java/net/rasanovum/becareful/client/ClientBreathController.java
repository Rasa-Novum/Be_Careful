package net.rasanovum.becareful.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ClientBreathController {

    private static long internalTick = 0L;
    private static long breathBurstEndTick = 0L;
    private static long nextBurstEmitTick = 0L;
    private static long nextBreathTick = 0L;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.isPaused()) return;

            ClientLevel level = client.level;
            Player player = client.player;
            if (level == null || player == null || player.isSpectator() || player.isCreative()) return;

            long time = ++internalTick;

            if (time < breathBurstEndTick) {
                if (time >= nextBurstEmitTick) {
                    ClientBreathSpawner.spawnBreath(client, level, player);
                    nextBurstEmitTick = time + 3;
                }
                return;
            }

            if (time < nextBreathTick) return;

            BlockPos pos = player.blockPosition();

            boolean isNearFrozenCampfire = net.rasanovum.becareful.blocks.FrozenCampfireBlock.PLAYERS_NEAR_COLD_FIRE.contains(player.getUUID());

            if (isNearFrozenCampfire) {
                breathBurstEndTick = time + 6;
                nextBurstEmitTick = time;
                nextBreathTick = time + 60;
                return;
            }

            if (player.getOffhandItem().is(Items.TORCH) || isNearHeatSource(level, pos)) {
                nextBreathTick = time + 40;
                return;
            }

            if (level.getBiome(pos).value().coldEnoughToSnow(pos)) {
                breathBurstEndTick = time + 6;
                nextBurstEmitTick = time;

                nextBreathTick = time + 60;
            } else {
                nextBreathTick = time + 40;
            }
        });
    }

    private static boolean isNearHeatSource(ClientLevel level, BlockPos pos) {
        int r = 8;
        for (BlockPos targetPos : BlockPos.betweenClosed(pos.offset(-r, -2, -r), pos.offset(r, 2, r))) {
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
}
