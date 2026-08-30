package net.rasanovum.becareful.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.rosetta.network.RosettaNetwork;
import net.rasanovum.rosetta.network.RosettaPacket;
import net.rasanovum.rosetta.util.GameRuleCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ChunkTameNetworking {
    private static final RosettaNetwork.Channel CHANNEL = RosettaNetwork.channel(BeCareful.MOD_ID);
    private static final Map<UUID, BlockPos> SHELTER_ANCHORS = new HashMap<>();
    private static final Set<UUID> SHELTER_INVALIDATED = new HashSet<>();
    private static boolean registered;

    private ChunkTameNetworking() {
    }

    public static void register() {
        if (registered) return;
        CHANNEL.clientbound("chunk_tame", ChunkTameSnapshotPacket.class, ChunkTameSnapshotPacket::write, ChunkTameSnapshotPacket::read,
                (packet, level, player) -> ClientChunkTameState.set(new ClientChunkTameState(
                        packet.chunkX(), packet.chunkZ(), packet.inhabitedTime(), packet.effectiveRequiredTicks(),
                        packet.rateMultiplier(), packet.settlementCategories(), packet.enabled(), packet.sleepMode(),
                        packet.shelter()
                ))
        );
        registered = true;
    }

    public static void syncPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos trackedAnchor = SHELTER_ANCHORS.get(player.getUUID());
        if (trackedAnchor != null && !(level.getBlockState(trackedAnchor).getBlock() instanceof BedBlock)) {
            invalidateShelter(player);
            sendNoShelterSnapshot(player);
            return;
        }
        if (trackedAnchor == null && SHELTER_INVALIDATED.contains(player.getUUID())) {
            sendNoShelterSnapshot(player);
            return;
        }

        BlockPos shelterAnchor = trackedAnchor == null ? player.blockPosition() : trackedAnchor;
        syncShelterSnapshot(player, shelterAnchor);
    }

    public static void syncPlayer(ServerPlayer player, BlockPos shelterAnchor) {
        SHELTER_ANCHORS.put(player.getUUID(), shelterAnchor.immutable());
        SHELTER_INVALIDATED.remove(player.getUUID());
        syncShelterSnapshot(player, shelterAnchor);
    }

    private static void syncShelterSnapshot(ServerPlayer player, BlockPos shelterAnchor) {
        ServerLevel level = player.serverLevel();
        int baseRequiredTicks = GameRuleCompat.get(level, BeCareful.RULE_CHUNK_TAME_TIME);
        ChunkTameManager.TameStatus status = ChunkTameManager.getStatus(level, player.blockPosition(), baseRequiredTicks);
        sendSnapshot(player, shelterAnchor, status, ShelterEvaluator.evaluate(level, shelterAnchor));
    }

    public static void clearShelterAnchor(ServerPlayer player) {
        SHELTER_ANCHORS.remove(player.getUUID());
        SHELTER_INVALIDATED.remove(player.getUUID());
    }

    public static void clearShelterAt(ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : level.players()) {
            BlockPos trackedAnchor = SHELTER_ANCHORS.get(player.getUUID());
            if (trackedAnchor != null && trackedAnchor.equals(pos)) {
                invalidateShelter(player);
                sendNoShelterSnapshot(player);
            }
        }
    }

    public static void syncPlayer(ServerPlayer player, BlockPos shelterAnchor, ChunkTameManager.TameStatus status,
                                  ShelterStatus shelter) {
        SHELTER_ANCHORS.put(player.getUUID(), shelterAnchor.immutable());
        SHELTER_INVALIDATED.remove(player.getUUID());
        sendSnapshot(player, shelterAnchor, status, shelter);
    }

    private static void invalidateShelter(ServerPlayer player) {
        SHELTER_ANCHORS.remove(player.getUUID());
        SHELTER_INVALIDATED.add(player.getUUID());
    }

    private static void sendNoShelterSnapshot(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int baseRequiredTicks = GameRuleCompat.get(level, BeCareful.RULE_CHUNK_TAME_TIME);
        sendSnapshot(player, player.blockPosition(),
                ChunkTameManager.getStatus(level, player.blockPosition(), baseRequiredTicks), null);
    }

    private static void sendSnapshot(ServerPlayer player, BlockPos shelterAnchor,
                                     ChunkTameManager.TameStatus status, ShelterStatus shelter) {
        ServerLevel level = player.serverLevel();
        RosettaNetwork.sendToPlayer(
                new ChunkTameSnapshotPacket(
                        level.getChunkAt(player.blockPosition()).getPos().x,
                        level.getChunkAt(player.blockPosition()).getPos().z,
                        status.inhabitedTime(), status.effectiveRequiredTicks(), status.rateMultiplier(),
                        status.settlementCategories(), BeCarefulConfig.doDifficultyFeatures,
                        BeCarefulConfig.sleepTamingMode, shelter
                ),
                player
        );
    }

    public record ChunkTameSnapshotPacket(int chunkX, int chunkZ, long inhabitedTime, int effectiveRequiredTicks,
                                          int rateMultiplier, int settlementCategories, boolean enabled,
                                          SleepTamingMode sleepMode,
                                          ShelterStatus shelter) implements RosettaPacket {
        public static void write(ChunkTameSnapshotPacket packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.chunkX());
            buffer.writeInt(packet.chunkZ());
            buffer.writeVarLong(packet.inhabitedTime());
            buffer.writeVarInt(packet.effectiveRequiredTicks());
            buffer.writeVarInt(packet.rateMultiplier());
            buffer.writeVarInt(packet.settlementCategories());
            buffer.writeBoolean(packet.enabled());
            buffer.writeVarInt(packet.sleepMode().ordinal());
            ShelterStatus shelter = packet.shelter();
            buffer.writeBoolean(shelter != null);
            if (shelter != null) {
                buffer.writeBlockPos(shelter.anchor());
                buffer.writeVarInt(shelter.minimumBlockLight());
                buffer.writeVarInt(shelter.maximumSkyLight());
                buffer.writeBoolean(shelter.lightSatisfied());
                buffer.writeBoolean(shelter.covered());
                buffer.writeBoolean(shelter.processed());
                buffer.writeBoolean(shelter.bounded());
                buffer.writeVarInt(shelter.volume().size());
                for (BlockPos pos : shelter.volume()) {
                    buffer.writeBlockPos(pos);
                }
            }
        }

        public static ChunkTameSnapshotPacket read(FriendlyByteBuf buffer) {
            return new ChunkTameSnapshotPacket(
                    buffer.readInt(), buffer.readInt(), buffer.readVarLong(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), readSleepMode(buffer), readShelter(buffer)
            );
        }

        private static SleepTamingMode readSleepMode(FriendlyByteBuf buffer) {
            int ordinal = buffer.readVarInt();
            SleepTamingMode[] modes = SleepTamingMode.values();
            return modes[Math.max(0, Math.min(modes.length - 1, ordinal))];
        }

        private static ShelterStatus readShelter(FriendlyByteBuf buffer) {
            if (!buffer.readBoolean()) {
                return null;
            }

            BlockPos anchor = buffer.readBlockPos();
            int minimumBlockLight = buffer.readVarInt();
            int maximumSkyLight = buffer.readVarInt();
            boolean lightSatisfied = buffer.readBoolean();
            boolean covered = buffer.readBoolean();
            boolean processed = buffer.readBoolean();
            boolean bounded = buffer.readBoolean();
            int count = Math.min(buffer.readVarInt(), 4096);
            java.util.ArrayList<BlockPos> volume = new java.util.ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                volume.add(buffer.readBlockPos());
            }
            return new ShelterStatus(anchor, minimumBlockLight, maximumSkyLight, lightSatisfied,
                    covered, processed, bounded, volume);
        }
    }
}
