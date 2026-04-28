package net.rasanovum.hardernether.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.rasanovum.hardernether.HarderNether;
import net.rasanovum.hardernether.HarderNetherConfig;
import net.rasanovum.hardernether.MessageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedSleepRestrictionMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void restrictSleep(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

            int CHUNK_UNSAFE_VARIANTS = HarderNetherConfig.chunkUnsafeVariants;
            int requiredTicks = serverLevel.getGameRules().getInt(HarderNether.RULE_CHUNK_TAME_TIME);
            long inhabitedTime = serverLevel.getChunkAt(pos).getInhabitedTime();

            if (inhabitedTime < requiredTicks) {
                player.displayClientMessage(
                        MessageManager.getRandomTranslatable("message.hardernether.bed_unsafe", CHUNK_UNSAFE_VARIANTS),
                        true
                );

                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
