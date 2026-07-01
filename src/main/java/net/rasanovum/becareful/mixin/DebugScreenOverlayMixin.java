package net.rasanovum.becareful.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.rasanovum.becareful.BeCarefulConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
    private void scrambleEndCoordinates(CallbackInfoReturnable<List<String>> cir) {
        List<String> infoLines = cir.getReturnValue();

        if (this.minecraft.level != null && this.minecraft.level.dimension() == Level.END && BeCarefulConfig.doEndFeatures) {
            for (int i = 0; i < infoLines.size(); i++) {
                String line = infoLines.get(i);

                if (line.startsWith("XYZ:") || line.startsWith("Block:") || line.startsWith("Chunk") || line.startsWith("Facing:")) {
                    RandomSource random = this.minecraft.level.random;
                    String corruptedLine;

                    if (line.startsWith("XYZ:")) {
                        corruptedLine = String.format("XYZ: %d / %d / %d", random.nextInt(99), random.nextInt(99), random.nextInt(99));
                    } else if (line.startsWith("Block:")) {
                        corruptedLine = "Block: ??? / ??? / ???";
                    } else if (line.startsWith("Chunk")) {
                        corruptedLine = "Chunk: ? ? ? in ? ? ?";
                    } else {
                        corruptedLine = "Facing: ??? (Towards ???) (?? / ??)";
                    }
                    infoLines.set(i, corruptedLine);
                }
            }
            cir.setReturnValue(infoLines);
        }
    }
}
