package net.rasanovum.becareful.mixin.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.rasanovum.becareful.effects.ChampionOfTheDarkEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

@Mixin(WardenSpawnTracker.class)
public abstract class WardenSpawnTrackerMixin {
    @Inject(method = "tryWarn", at = @At("HEAD"), cancellable = true)
    private static void beCareful$protectChampion(ServerLevel level, BlockPos pos, ServerPlayer player,
                                                 CallbackInfoReturnable<OptionalInt> cir) {
        if (ChampionOfTheDarkEffect.protects(player)) {
            cir.setReturnValue(OptionalInt.empty());
        }
    }

    @Inject(method = "getNearbyPlayers", at = @At("RETURN"), cancellable = true)
    private static void beCareful$excludeChampions(ServerLevel level, BlockPos pos,
                                                  CallbackInfoReturnable<List<ServerPlayer>> cir) {
        List<ServerPlayer> players = new ArrayList<>(cir.getReturnValue());
        players.removeIf(ChampionOfTheDarkEffect::protects);
        cir.setReturnValue(players);
    }
}
