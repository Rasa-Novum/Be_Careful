package net.rasanovum.hardernether;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashSet;
import java.util.Set;

public class PortalState extends SavedData {
    private final Set<BlockPos> authorizedPortals = new HashSet<>();

    public static PortalState get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(PortalState::load, PortalState::new, "harder_nether_portals");
    }

    public void addPortal(BlockPos pos) {
        authorizedPortals.add(pos.immutable());
        setDirty();
    }

    public boolean isAuthorized(BlockPos pos, ServerLevel level) {
        boolean isNether = level.dimension().equals(Level.NETHER);
        double coordinateScale = level.dimensionType().coordinateScale();
        long checkX = (long) (pos.getX() * (isNether ? (1.0 / coordinateScale) : 1.0));
        long checkZ = (long) (pos.getZ() * (isNether ? (1.0 / coordinateScale) : 1.0));

        // debug
        ServerPlayer debugPlayer = (ServerPlayer) level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);
        if (debugPlayer != null) {
            debugPlayer.displayClientMessage(Component.literal("Checking Coords: " + checkX + ", " + checkZ).withStyle(ChatFormatting.GRAY), false);
        }

        for (BlockPos authPos : authorizedPortals) {
            long dx = authPos.getX() - checkX;
            long dz = authPos.getZ() - checkZ;
            long distSq = (dx * dx + dz * dz);

            if ((dx * dx + dz * dz) <= 512) { //this can likely be reduced to 256 when the 1:1 nether datapack is implemented, but for now this works in most cases (1024 would catch some edge cases tho)
                return true;
            } else {
                // debug: show where nearest anchor is
                if (debugPlayer != null) {
                    debugPlayer.displayClientMessage(Component.literal("Anchor found at " + authPos.getX() + ", " + authPos.getZ() + " (DistSq: " + distSq + ")").withStyle(ChatFormatting.RED), false);
                }
            }
        }
        return false;
    }

    public static PortalState load(CompoundTag nbt) {
        PortalState state = new PortalState();
        ListTag list = nbt.getList("portals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            state.authorizedPortals.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (BlockPos pos : authorizedPortals) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        nbt.put("portals", list);
        return nbt;
    }
}
