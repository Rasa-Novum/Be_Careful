package net.rasanovum.becareful.mixin.difficulty;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.taming.ChunkTameManager;
import net.rasanovum.becareful.taming.ChunkTameNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelSetBlockMixin {

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void invalidateChunkTameProfile(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            BlockState previousState = level.getBlockState(pos);
            if (previousState.getBlock() instanceof BedBlock && !(state.getBlock() instanceof BedBlock)) {
                ChunkTameNetworking.clearShelterAt(serverLevel, pos);
            }
            if (BeCarefulConfig.doChunkTameAcceleration
                    && (ChunkTameManager.isSettlementBlock(previousState) || ChunkTameManager.isSettlementBlock(state))) {
                ChunkTameManager.invalidate(serverLevel, pos);
            }
        }
    }
}
