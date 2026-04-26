package net.rasanovum.hardernether.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.rasanovum.hardernether.HarderNether;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public abstract class PortalIgnitionMixin {

    private static final TagKey<Structure> RUINED_PORTALS =
            TagKey.create(Registries.STRUCTURE, new ResourceLocation("minecraft", "ruined_portal"));

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void stopPortalForming(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameRules().getBoolean(HarderNether.RULE_ONLY_RUINED_PORTALS)) {

                StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(pos, RUINED_PORTALS);

                if (start == null || !start.isValid()) {
                    ci.cancel();
                }
            }
        }
    }
}
