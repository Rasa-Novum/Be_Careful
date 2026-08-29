package net.rasanovum.becareful.taming;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.rosetta.network.RosettaNetwork;
import net.rasanovum.rosetta.network.RosettaPacket;
import net.rasanovum.rosetta.util.GameRuleCompat;

public final class ChunkTameNetworking {
    private static final RosettaNetwork.Channel CHANNEL = RosettaNetwork.channel(BeCareful.MOD_ID);
    private static boolean registered;

    private ChunkTameNetworking() {
    }

    public static void register() {
        if (registered) return;
        CHANNEL.clientbound("chunk_tame", ChunkTameSnapshotPacket.class, ChunkTameSnapshotPacket::write, ChunkTameSnapshotPacket::read,
                (packet, level, player) -> ClientChunkTameState.set(new ClientChunkTameState(
                        packet.chunkX(), packet.chunkZ(), packet.inhabitedTime(), packet.effectiveRequiredTicks(),
                        packet.rateMultiplier(), packet.settlementCategories(), packet.enabled()
                ))
        );
        registered = true;
    }

    public static void syncPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int baseRequiredTicks = GameRuleCompat.get(level, BeCareful.RULE_CHUNK_TAME_TIME);
        ChunkTameManager.TameStatus status = ChunkTameManager.getStatus(level, player.blockPosition(), baseRequiredTicks);
        RosettaNetwork.sendToPlayer(
                new ChunkTameSnapshotPacket(
                        level.getChunkAt(player.blockPosition()).getPos().x,
                        level.getChunkAt(player.blockPosition()).getPos().z,
                        status.inhabitedTime(), status.effectiveRequiredTicks(), status.rateMultiplier(),
                        status.settlementCategories(), BeCarefulConfig.doDifficultyFeatures
                ),
                player
        );
    }

    public record ChunkTameSnapshotPacket(int chunkX, int chunkZ, long inhabitedTime, int effectiveRequiredTicks, int rateMultiplier, int settlementCategories, boolean enabled) implements RosettaPacket {
        public static void write(ChunkTameSnapshotPacket packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.chunkX());
            buffer.writeInt(packet.chunkZ());
            buffer.writeVarLong(packet.inhabitedTime());
            buffer.writeVarInt(packet.effectiveRequiredTicks());
            buffer.writeVarInt(packet.rateMultiplier());
            buffer.writeVarInt(packet.settlementCategories());
            buffer.writeBoolean(packet.enabled());
        }

        public static ChunkTameSnapshotPacket read(FriendlyByteBuf buffer) {
            return new ChunkTameSnapshotPacket(
                    buffer.readInt(), buffer.readInt(), buffer.readVarLong(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean()
            );
        }
    }
}