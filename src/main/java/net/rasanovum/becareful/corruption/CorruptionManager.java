package net.rasanovum.becareful.corruption;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.BeCarefulHooks;
import net.rasanovum.rosetta.network.RosettaNetwork;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CorruptionManager {
    private static final Map<UUID, Float> VALUES = new HashMap<>();
    private static final Set<UUID> OWNED_DARKNESS = new HashSet<>();

    private CorruptionManager() {}

    public static void expose(ServerPlayer player) {
        set(player, 1.0F);
    }

    public static void tick(ServerPlayer player, boolean inDeepDark, int deepDarkTime, boolean timerIncreasing) {
        float current = get(player);
        float next = inDeepDark
                ? corruptionAt(deepDarkTime)
                : current - (1.0F / 40.0F);
        boolean warningReached = inDeepDark && deepDarkTime >= BeCareful.DD_WARN_TICKS;
        boolean dangerReached = inDeepDark && deepDarkTime >= BeCareful.DD_DANGER_TICKS;

        if (next <= 0.0F) {
            if (!warningReached) {
                cleanse(player);
            } else {
                if (current > 0.0F) {
                    set(player, 0.0F);
                }
                updateStageEffects(player, warningReached, dangerReached, timerIncreasing);
            }
            return;
        }

        if (next != current) {
            set(player, next);
        }

        updateStageEffects(player, warningReached, dangerReached, timerIncreasing);
    }

    private static void updateStageEffects(
            ServerPlayer player,
            boolean warningReached,
            boolean dangerReached,
        boolean timerIncreasing
    ) {
        if (warningReached && timerIncreasing) {
            addDarknessEffect(player);
        } else {
            removeDarknessEffect(player);
        }

        if (dangerReached) {
            addCorruptionEffect(player);
        } else {
            removeCorruptionEffect(player);
        }
    }

    private static float corruptionAt(int deepDarkTime) {
        int warningTicks = BeCareful.DD_WARN_TICKS;
        int dangerTicks = BeCareful.DD_DANGER_TICKS;
        if (deepDarkTime <= warningTicks) {
            return 0.0F;
        }
        if (dangerTicks <= warningTicks) {
            return 1.0F;
        }
        return Math.min(1.0F, (deepDarkTime - warningTicks) / (float) (dangerTicks - warningTicks));
    }

    public static float get(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return get(player);
        }
        return 1.0F;
    }

    public static float get(ServerPlayer player) {
        return VALUES.getOrDefault(player.getUUID(), 0.0F);
    }

    public static void cleanse(ServerPlayer player) {
        float previous = VALUES.getOrDefault(player.getUUID(), 0.0F);
        VALUES.remove(player.getUUID());
        removeEffects(player);
        if (previous > 0.0F) {
            send(player, 0.0F);
        }
    }

    public static void sync(ServerPlayer player) {
        send(player, get(player));
    }

    public static void logState(ServerPlayer player, boolean protectedByLight, boolean inDeepDark) {
        float value = get(player);
        int deepDarkTime = BeCarefulHooks.deepDarkTime(player.getUUID());
        int warningRemaining = Math.max(0, BeCareful.DD_WARN_TICKS - deepDarkTime);
        int dangerRemaining = Math.max(0, BeCareful.DD_DANGER_TICKS - deepDarkTime);
        String stage = protectedByLight
                ? "protected"
                : !inDeepDark
                ? "outside"
                : deepDarkTime >= BeCareful.DD_DANGER_TICKS
                ? "danger"
                : deepDarkTime >= BeCareful.DD_WARN_TICKS
                ? "warning"
                : "entry";
        BeCareful.LOGGER.info(
                "Corruption state: player={}, value={}, stage={}, deepDarkTime={}t, warningRemaining={}t, dangerRemaining={}t, inDeepDark={}, protectedByLight={}",
                player.getGameProfile().getName(),
                Math.round(value * 1000.0F) / 1000.0F,
                stage,
                deepDarkTime,
                warningRemaining,
                dangerRemaining,
                inDeepDark,
                protectedByLight
        );
    }

    public static void clear(UUID playerId) {
        VALUES.remove(playerId);
        OWNED_DARKNESS.remove(playerId);
    }

    private static void set(ServerPlayer player, float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        Float previous = VALUES.put(player.getUUID(), clamped);
        if (previous == null || Math.abs(previous - clamped) >= 0.001F) {
            send(player, clamped);
        }
    }

    private static void addCorruptionEffect(ServerPlayer player) {
        int interval = Math.max(1, BeCarefulConfig.corruptionDamageIntervalTicks);
        /*? if <1.21 {*/
        /*MobEffectInstance current = player.getEffect(BeCareful.CORRUPTION);
        *//*?} else {*/
        MobEffectInstance current = player.getEffect(BeCareful.CORRUPTION_HOLDER);
        /*?}*/
        if (current != null && current.getDuration() > interval) {
            return;
        }

        /*? if <1.21 {*/
        /*player.addEffect(new MobEffectInstance(BeCareful.CORRUPTION, interval * 2, 0, false, false));
        *//*?} else {*/
        player.addEffect(new MobEffectInstance(BeCareful.CORRUPTION_HOLDER, interval * 2, 0, false, false));
        /*?}*/
    }

    private static void addBlindnessEffect(ServerPlayer player, float corruption) {
        int duration = Math.max(2, Math.min(20, Math.round(corruption * 40.0F)));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false));
    }

    private static void removeCorruptionEffect(ServerPlayer player) {
        /*? if <1.21 {*/
        /*player.removeEffect(BeCareful.CORRUPTION);
        *//*?} else {*/
        player.removeEffect(BeCareful.CORRUPTION_HOLDER);
        /*?}*/
    }

    private static void addDarknessEffect(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(MobEffects.DARKNESS);
        if (current == null) {
            // Match the Warden's darkness duration while letting the renderer handle the transition.
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false));
            OWNED_DARKNESS.add(player.getUUID());
        } else if (OWNED_DARKNESS.contains(player.getUUID()) && current.getDuration() <= 40) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false));
        }
    }

    private static void removeDarknessEffect(ServerPlayer player) {
        if (OWNED_DARKNESS.remove(player.getUUID())) {
            player.removeEffect(MobEffects.DARKNESS);
        }
    }

    private static void removeEffects(ServerPlayer player) {
        removeCorruptionEffect(player);
        removeDarknessEffect(player);
    }

    private static void send(ServerPlayer player, float value) {
        RosettaNetwork.sendToPlayer(new CorruptionUpdatePacket(value), player);
    }
}
