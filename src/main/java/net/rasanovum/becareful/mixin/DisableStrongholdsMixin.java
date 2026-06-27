package net.rasanovum.becareful.mixin;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Structure.class)
public class DisableStrongholdsMixin {

    @Inject(
            method = "findValidGenerationPoint(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockStrongholdAnchorPoints(Structure.GenerationContext context, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        Structure structureInstance = (Structure) (Object) this;

        if (structureInstance.type() != null) {
            var registryKey = net.minecraft.core.registries.BuiltInRegistries.STRUCTURE_TYPE.getKey(structureInstance.type());

            if (registryKey != null && registryKey.getPath().equals("stronghold")) {
                cir.setReturnValue(Optional.empty());
            }
        }
    }
}
