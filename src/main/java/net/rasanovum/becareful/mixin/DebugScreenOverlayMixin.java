package net.rasanovum.becareful.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import net.rasanovum.becareful.corruption.ClientDeepDarkDebugState;
import net.rasanovum.becareful.taming.ClientChunkTameState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
    private void addBeCarefulDebugInformation(CallbackInfoReturnable<List<String>> cir) {
        List<String> infoLines = cir.getReturnValue();

        scrambleEndCoordinates(infoLines);
        addChunkTameInformation(infoLines);
        addDeepDarkInformation(infoLines);

        cir.setReturnValue(infoLines);
    }

    @Unique
    private void scrambleEndCoordinates(List<String> infoLines) {
        if (this.minecraft.level == null || this.minecraft.level.dimension() != Level.END || !BeCarefulConfig.doEndFeatures) {
            return;
        }

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
    }

    @Unique
    private void addChunkTameInformation(List<String> infoLines) {
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }

        ChunkPos currentChunk = new ChunkPos(this.minecraft.player.blockPosition());
        ClientChunkTameState snapshot = ClientChunkTameState.get();

        if (snapshot == null || snapshot.chunkX() != currentChunk.x || snapshot.chunkZ() != currentChunk.z) {
            infoLines.add("Taming: syncing server data...");
        } else if (!snapshot.enabled()) {
            infoLines.add("Taming: disabled");
        } else {
            double progress = snapshot.effectiveRequiredTicks() <= 0 ? 100.0 : Math.min(100.0, snapshot.inhabitedTime() * 100.0 / snapshot.effectiveRequiredTicks());
            String state = snapshot.inhabitedTime() >= snapshot.effectiveRequiredTicks() ? "TAMED" : "UNTAMED";
            infoLines.add(String.format(Locale.ROOT, "Taming: %,d / %,d (%dx; %d/4 types; %.1f%%; %s)",
                    snapshot.inhabitedTime(), snapshot.effectiveRequiredTicks(), snapshot.rateMultiplier(), snapshot.settlementCategories(), progress, state
            ));
        }
    }

    @Unique
    private void addDeepDarkInformation(List<String> infoLines) {
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }

        ClientDeepDarkDebugState debugState = ClientDeepDarkDebugState.get();
        if (debugState == null) {
            infoLines.add("Deep Dark: syncing server data...");
            return;
        }

        String stage = debugState.protectedByLight()
                ? "PROTECTED"
                : !debugState.inDeepDark()
                ? "OUTSIDE"
                : debugState.deepDarkTime() >= BeCareful.DD_DANGER_TICKS
                ? "DANGER"
                : debugState.deepDarkTime() >= BeCareful.DD_WARN_TICKS
                ? "WARNING"
                : "ENTRY";
        infoLines.add(String.format(Locale.ROOT, "Deep Dark: %dt (corruption %.3f; warning %dt; danger %dt; %s)",
                debugState.deepDarkTime(), ClientCorruptionState.get(), debugState.warningRemaining(), debugState.dangerRemaining(), stage
        ));
    }
}
