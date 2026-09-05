package net.rasanovum.becareful.light;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.effects.ChampionOfTheDarkEffect;
import net.rasanovum.becareful.util.SculkHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class WardenDeathWaveManager {
    private static final Map<ServerLevel, List<Wave>> WAVES = new WeakHashMap<>();
    private static final int BLOCK_BUDGET = 4096;

    private WardenDeathWaveManager() {}

    public static void create(ServerLevel level, Vec3 center) {
        long start = level.getGameTime();
        int contraction = Math.max(1, BeCarefulConfig.wardenDeathWaveContractionTicks);
        int rebound = Math.max(1, BeCarefulConfig.wardenDeathWaveReboundTicks);
        int fade = Math.max(1, BeCarefulConfig.wardenDeathWaveFadeTicks);
        int radius = Math.multiplyExact(Math.max(1, BeCarefulConfig.lightFieldRadius),
                Math.max(1, Math.min(8, BeCarefulConfig.wardenDeathWaveRadiusMultiplier)));
        LightField field = new LightField(UUID.randomUUID(), center, radius, start,
                start + contraction + (long) rebound + fade, false, contraction, rebound);
        WAVES.computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new Wave(field, Math.max(1, BeCarefulConfig.championOfTheDarkDurationTicks)));
        LightFieldNetworking.sync(level);
    }

    public static List<LightField> activeFields(ServerLevel level) {
        return WAVES.getOrDefault(level, List.of()).stream().map(wave -> wave.field)
                .filter(field -> field.expiresAt() > level.getGameTime()).toList();
    }

    public static void tick(ServerLevel level) {
        List<Wave> waves = WAVES.get(level);
        if (waves == null) return;
        long time = level.getGameTime();
        boolean changed = false;
        for (var iterator = waves.iterator(); iterator.hasNext();) {
            Wave wave = iterator.next();
            if (!level.hasChunkAt(BlockPos.containing(wave.field.center()))) {
                iterator.remove();
                changed = true;
                continue;
            }
            wave.tick(level, time);
            if (time >= wave.field.expiresAt() && wave.pending.isEmpty()) {
                iterator.remove();
                changed = true;
            }
        }
        if (waves.isEmpty()) WAVES.remove(level);
        if (changed) LightFieldNetworking.sync(level);
    }

    private static final class Wave {
        private final LightField field;
        private final int effectDuration;
        private final Set<UUID> rewarded = new HashSet<>();
        private final ArrayDeque<SphericalShell> pending = new ArrayDeque<>();
        private double sweptRadius = -1;

        private Wave(LightField field, int effectDuration) {
            this.field = field;
            this.effectDuration = effectDuration;
        }

        private void tick(ServerLevel level, long time) {
            if (time < field.startedAt() + field.contractionTicks()) return;
            double radius = field.stateAt(time).radius();
            if (radius > sweptRadius) {
                if (BeCarefulConfig.replaceSculkInLightFields) {
                    pending.add(new SphericalShell(field.center(), sweptRadius, radius,
                            level.getMinBuildHeight(), level.getMaxBuildHeight() - 1));
                }
                sweptRadius = radius;
            }
            if (time <= field.startedAt() + field.contractionTicks() + (long) field.reboundTicks()) {
                for (ServerPlayer player : level.players()) {
                    if (player.isAlive() && !player.isSpectator() && field.contains(player, time)
                            && rewarded.add(player.getUUID())) {
                        ChampionOfTheDarkEffect.grant(player, effectDuration);
                    }
                }
            }
            if (!BeCarefulConfig.replaceSculkInLightFields) {
                pending.clear();
                return;
            }
            int budget = BLOCK_BUDGET;
            while (!pending.isEmpty() && budget > 0) {
                SphericalShell shell = pending.peek();
                if (!shell.hasNext()) {
                    pending.remove();
                    continue;
                }
                BlockPos pos = shell.next();
                budget--;
                if (!level.hasChunkAt(pos)) continue;
                var replacement = SculkHelper.replacementFor(level.getBlockState(pos));
                if (replacement != null) level.setBlockAndUpdate(pos, replacement);
            }
        }
    }
}
