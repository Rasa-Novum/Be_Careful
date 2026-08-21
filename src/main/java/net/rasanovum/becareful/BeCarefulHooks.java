package net.rasanovum.becareful;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.rasanovum.becareful.spawning.EndPhantomSpawner;
import net.rasanovum.becareful.corruption.CorruptionManager;
import net.rasanovum.becareful.light.LightFieldManager;
import net.rasanovum.becareful.light.LightFieldNetworking;
import net.rasanovum.becareful.util.AdvancementManager;
import net.rasanovum.becareful.util.ChunkTameManager;
import net.rasanovum.becareful.util.ColdEnvironmentManager;
import net.rasanovum.becareful.util.MessageManager;
import net.rasanovum.rosetta.event.ServerHooks;
import net.rasanovum.rosetta.util.EntityCompat;
import net.rasanovum.rosetta.util.GameRuleCompat;

import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BeCarefulHooks {
    private static final Map<UUID, Integer> DEEP_DARK_TIMERS = new HashMap<>();
    private static final Map<UUID, Integer> NETHER_TIMERS = new HashMap<>();
    private static final Map<UUID, Integer> MESSAGE_SCHEDULE = new HashMap<>();
    private static boolean registered;

    private BeCarefulHooks() {}

    public static void register() {
        if (registered) return;
        ServerHooks.register(new ServerHooks.Callbacks() {
            @Override
            public void onServerLevelTick(ServerLevel level) {
                tickLevel(level);
            }

            @Override
            public void onPlayerLeave(ServerPlayer player) {
                clearPlayerState(player.getUUID());
            }

            @Override
            public void onPlayerJoin(ServerPlayer player) {
                LightFieldNetworking.syncPlayer(player);
                CorruptionManager.sync(player);
            }

            @Override
            public void onPlayerChangedDimension(ServerPlayer player) {
                LightFieldNetworking.syncPlayer(player);
                CorruptionManager.sync(player);
            }

            @Override
            public void onDataPackReload(net.minecraft.server.MinecraftServer server) {
                BeCareful.reloadConfig();
            }
        });
        registered = true;
    }

    public static int deepDarkTime(UUID playerId) {
        return DEEP_DARK_TIMERS.getOrDefault(playerId, 0);
    }

    private static void tickLevel(ServerLevel level) {
        int currentTick = level.getServer().getTickCount();

        LightFieldManager.tick(level);

        for (ServerPlayer player : level.players()) {
            tickPlayer(level, player, currentTick);
        }

        if (BeCarefulConfig.doEndFeatures) {
            EndPhantomSpawner.tick(level);
        }
        if (BeCarefulConfig.doFrozenFeatures) {
            ColdEnvironmentManager.tick(level);
        }
    }

    private static void tickPlayer(ServerLevel level, ServerPlayer player, int currentTick) {
        int requiredTicks = GameRuleCompat.get(player.serverLevel(), BeCareful.RULE_CHUNK_TAME_TIME);
        long inhabitedTime = player.serverLevel().getChunkAt(player.blockPosition()).getInhabitedTime();
        int effectiveRequiredTicks = ChunkTameManager.getEffectiveTameTime(
                player.serverLevel(), player.blockPosition(), requiredTicks
        );
        if (inhabitedTime == effectiveRequiredTicks && BeCarefulConfig.doDifficultyFeatures) {
            AdvancementManager.award(player, AdvancementManager.CHUNK_TAMED);
            EntityCompat.displayClientMessage(player,
                    MessageManager.getRandomTranslatable("message.be_careful.chunk_tamed", BeCareful.CHUNK_TAME_VARIANTS)
                            .copy().withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        if (currentTick % 20 == 0) {
            AdvancementManager.checkRuinedPortal(player);
        }

        UUID uuid = player.getUUID();
        Integer scheduledMessageTick = MESSAGE_SCHEDULE.get(uuid);
        if (scheduledMessageTick != null && currentTick >= scheduledMessageTick) {
            EntityCompat.displayClientMessage(player,
                    MessageManager.getRandomTranslatable("message.be_careful.totem_cleanse", BeCareful.TOTEM_VARIANTS)
                            .copy().withStyle(ChatFormatting.GREEN),
                    true
            );
            MESSAGE_SCHEDULE.remove(uuid);
        }

        tickDeepDark(player, currentTick);
        tickNether(player);
    }

    private static void tickDeepDark(ServerPlayer player, int currentTick) {
        ServerLevel level = player.serverLevel();
        boolean inDeepDark = level.getBiome(player.blockPosition()).is(Biomes.DEEP_DARK)
                && BeCarefulConfig.doDeepDarkFeatures;
        boolean protectedByLight = LightFieldManager.contains(level, player);

        if (inDeepDark && !protectedByLight) {
            int time = DEEP_DARK_TIMERS.getOrDefault(player.getUUID(), 0) + 1;
            DEEP_DARK_TIMERS.put(player.getUUID(), time);
            BeCareful.DEEP_DARK.tick(player, time);
        } else if (inDeepDark) {
            int decrement = Math.max(0, BeCarefulConfig.lightFieldTimerDecrement);
            int time = decrement == 0
                    ? 0
                    : Math.max(0, DEEP_DARK_TIMERS.getOrDefault(player.getUUID(), 0) - decrement);
            DEEP_DARK_TIMERS.put(player.getUUID(), time);
        } else {
            DEEP_DARK_TIMERS.remove(player.getUUID());
        }

        CorruptionManager.tick(
                player,
                inDeepDark,
                DEEP_DARK_TIMERS.getOrDefault(player.getUUID(), 0),
                inDeepDark && !protectedByLight
        );
        if (currentTick % 20 == 0) {
            CorruptionManager.logState(player, protectedByLight, inDeepDark);
        }
    }

    private static void tickNether(ServerPlayer player) {
        if (player.level().dimension().equals(Level.NETHER) && BeCarefulConfig.doNetherFeatures) {
            int time = NETHER_TIMERS.getOrDefault(player.getUUID(), 0) + 1;
            NETHER_TIMERS.put(player.getUUID(), time);
            BeCareful.NETHER.tick(player, time);
        }
    }

    private static void clearPlayerState(UUID playerId) {
        DEEP_DARK_TIMERS.remove(playerId);
        NETHER_TIMERS.remove(playerId);
        MESSAGE_SCHEDULE.remove(playerId);
        CorruptionManager.clear(playerId);
    }
}
