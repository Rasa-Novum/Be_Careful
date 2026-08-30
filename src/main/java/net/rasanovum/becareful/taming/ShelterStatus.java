package net.rasanovum.becareful.taming;

import net.minecraft.core.BlockPos;

import java.util.List;

public record ShelterStatus(BlockPos anchor, int minimumBlockLight, int maximumSkyLight,
                            boolean lightSatisfied, boolean covered, boolean processed,
                            boolean bounded, List<BlockPos> volume) {
    public ShelterStatus {
        anchor = anchor.immutable();
        volume = List.copyOf(volume);
    }

    public boolean isSafe() {
        return lightSatisfied && covered && processed && bounded;
    }
}
