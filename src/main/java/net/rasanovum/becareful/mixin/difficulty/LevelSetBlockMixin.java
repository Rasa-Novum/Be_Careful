package net.rasanovum.becareful.mixin.difficulty;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.taming.ChunkTameManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelSetBlockMixin {

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void invalidateChunkTameProfile(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;

        if (!level.isClientSide && level instanceof ServerLevel serverLevel && BeCarefulConfig.doChunkTameAcceleration) {
            BlockState previousState = level.getBlockState(pos);
            if (ChunkTameManager.isSettlementBlock(previousState) || ChunkTameManager.isSettlementBlock(state)) {
                ChunkTameManager.invalidate(serverLevel, pos);
            }
        }
    }
}
