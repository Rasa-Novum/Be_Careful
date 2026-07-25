package net.rasanovum.becareful.portals;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.rosetta.attachment.LevelAttachmentKey;
import net.rasanovum.rosetta.attachment.RosettaAttachments;
import net.rasanovum.rosetta.util.EntityCompat;
import net.rasanovum.rosetta.util.GameRuleCompat;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;

public final class PortalState {
    private static final Codec<Set<BlockPos>> CODEC =
            BlockPos.CODEC.listOf().xmap(HashSet::new, ArrayList::new);
    private static final LevelAttachmentKey<Set<BlockPos>> AUTHORIZED_PORTALS =
            RosettaAttachments.level(BeCareful.MOD_ID).persistent("authorized_portals", HashSet::new, CODEC);

    private final ServerLevel level;
    private final Set<BlockPos> authorizedPortals;

    private PortalState(ServerLevel level) {
        this.level = level.getServer().overworld();
        this.authorizedPortals = AUTHORIZED_PORTALS.getOrCreate(this.level);
    }

    public static void bootstrap() {
    }

    public static PortalState get(ServerLevel level) {
        return new PortalState(level);
    }

    public void addPortal(BlockPos pos) {
        authorizedPortals.add(pos.immutable());
        AUTHORIZED_PORTALS.markDirty(level);
    }

    public boolean isAuthorized(BlockPos pos, ServerLevel queryLevel) {
        long checkX = pos.getX();
        long checkZ = pos.getZ();
        boolean portalGameRule = GameRuleCompat.get(queryLevel, BeCareful.RULE_DO_PORTAL_DEBUG);
        ServerPlayer debugPlayer = (ServerPlayer) queryLevel.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);

        if (debugPlayer != null && portalGameRule) {
            EntityCompat.displayClientMessage(debugPlayer,
                    Component.literal("Checking Coords: " + checkX + ", " + checkZ).withStyle(ChatFormatting.GRAY), false);
        }

        for (BlockPos authPos : authorizedPortals) {
            long dx = authPos.getX() - checkX;
            long dz = authPos.getZ() - checkZ;
            long distSq = dx * dx + dz * dz;

            if (distSq <= 256) {
                return true;
            }
            if (debugPlayer != null && portalGameRule) {
                EntityCompat.displayClientMessage(debugPlayer,
                        Component.literal("Anchor found at " + authPos.getX() + ", " + authPos.getZ()
                                + " (DistSq: " + distSq + ")").withStyle(ChatFormatting.RED), false);
            }
        }
        return false;
    }
}
