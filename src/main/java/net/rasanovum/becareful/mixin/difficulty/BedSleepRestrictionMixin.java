package net.rasanovum.becareful.mixin.difficulty;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.util.ChunkTameManager;
import net.rasanovum.becareful.util.MessageManager;
import net.rasanovum.rosetta.util.GameRuleCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedSleepRestrictionMixin {

    /*? if <1.21 {*/
    /*@Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void restrictSleep(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
    *//*?} else {*/
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void restrictSleep(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
    /*?}*/
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && BeCarefulConfig.doDifficultyFeatures) {

            int CHUNK_UNSAFE_VARIANTS = BeCarefulConfig.chunkUnsafeVariants;
            int requiredTicks = GameRuleCompat.get(serverLevel, BeCareful.RULE_CHUNK_TAME_TIME);
            long inhabitedTime = serverLevel.getChunkAt(pos).getInhabitedTime();
            int effectiveRequiredTicks = ChunkTameManager.getEffectiveTameTime(serverLevel, pos, requiredTicks);

            if (inhabitedTime < effectiveRequiredTicks) {
                player.displayClientMessage(
                        MessageManager.getRandomTranslatable("message.be_careful.bed_unsafe", CHUNK_UNSAFE_VARIANTS),
                        true
                );

                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
