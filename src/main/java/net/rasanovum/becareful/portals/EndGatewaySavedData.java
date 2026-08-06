package net.rasanovum.becareful.portals;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.rosetta.attachment.LevelAttachmentKey;
import net.rasanovum.rosetta.attachment.RosettaAttachments;

public final class EndGatewaySavedData {
    private static final LevelAttachmentKey<Integer> GENERATED_COUNT =
            RosettaAttachments.level(BeCareful.MOD_ID).persistent("end_gateway_count", () -> 0, Codec.INT);

    private final ServerLevel level;

    private EndGatewaySavedData(ServerLevel level) {
        this.level = level;
    }

    public static void bootstrap() {
    }

    public int getGeneratedCount() {
        return GENERATED_COUNT.getOrCreate(level);
    }

    public void incrementGeneratedCount() {
        GENERATED_COUNT.set(level, getGeneratedCount() + 1);
    }

    public static EndGatewaySavedData get(ServerLevel level) {
        return new EndGatewaySavedData(level.getServer().overworld());
    }
}
