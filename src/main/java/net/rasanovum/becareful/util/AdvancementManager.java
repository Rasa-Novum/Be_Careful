package net.rasanovum.becareful.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.rasanovum.rosetta.util.EntityCompat;
import net.rasanovum.rosetta.util.RegistryCompat;

public final class AdvancementManager {
    public static final String EYES_UP_GUARDIAN = "be-careful:adventure/eyes_up_guardian";
    public static final String CHUNK_TAMED = "be-careful:backend/chunk_tamed";
    public static final String RUINED_PORTAL_VISITED = "be-careful:backend/ruined_portal_visited";
    public static final String ENDER_EYE_USED_IN_END = "be-careful:backend/ender_eye_used_in_end";
    public static final String COLD_BIOME_ENTERED = "be-careful:backend/cold_biome_entered";
    public static final String FOLLOW_ENDER_EYE = "minecraft:story/follow_ender_eye";

    private static final TagKey<Structure> RUINED_PORTALS =
            TagKey.create(Registries.STRUCTURE, RegistryCompat.getLocation("minecraft", "ruined_portal"));

    private AdvancementManager() {
    }

    public static void award(ServerPlayer player, String advancement) {
        EntityCompat.awardAdvancement(player, advancement);
    }

    public static void checkRuinedPortal(ServerPlayer player) {
        StructureStart start = player.serverLevel().structureManager()
                .getStructureWithPieceAt(player.blockPosition(), RUINED_PORTALS);
        if (start != null && start.isValid()) {
            award(player, RUINED_PORTAL_VISITED);
        }
    }
}
