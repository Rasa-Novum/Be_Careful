package net.rasanovum.becareful.light;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import net.rasanovum.becareful.corruption.CorruptionUpdatePacket;
import net.rasanovum.rosetta.network.RosettaNetwork;
import net.rasanovum.rosetta.network.RosettaPacket;

import java.util.List;

public final class LightFieldNetworking {
    private static final RosettaNetwork.Channel CHANNEL = RosettaNetwork.channel(BeCareful.MOD_ID);
    private static boolean registered;

    private LightFieldNetworking() {}

    public static void register() {
        if (registered) return;
        CHANNEL.clientbound(
                "light_fields", LightFieldSnapshotPacket.class,
                LightFieldSnapshotPacket::write, LightFieldSnapshotPacket::read,
                (packet, level, player) -> ClientLightFieldState.set(packet.fields())
        );
        CHANNEL.clientbound(
                "corruption", CorruptionUpdatePacket.class,
                CorruptionUpdatePacket::write, CorruptionUpdatePacket::read,
                (packet, level, player) -> ClientCorruptionState.set(packet.value())
        );
        registered = true;
    }

    public static void sync(ServerLevel level) {
        LightFieldSnapshotPacket packet = new LightFieldSnapshotPacket(LightFieldManager.activeFields(level));
        for (ServerPlayer player : level.players()) {
            RosettaNetwork.sendToPlayer(packet, player);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        RosettaNetwork.sendToPlayer(
                new LightFieldSnapshotPacket(LightFieldManager.activeFields(player.serverLevel())), player
        );
    }

    public record LightFieldSnapshotPacket(List<LightField> fields) implements RosettaPacket {
        public LightFieldSnapshotPacket {
            fields = List.copyOf(fields);
        }

        public static void write(LightFieldSnapshotPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.fields.size());
            for (LightField field : packet.fields) {
                buffer.writeUUID(field.id());
                buffer.writeBlockPos(field.center());
                buffer.writeVarInt(field.radius());
                buffer.writeVarLong(field.expiresAt());
            }
        }

        public static LightFieldSnapshotPacket read(FriendlyByteBuf buffer) {
            int count = Math.min(buffer.readVarInt(), 1024);
            java.util.ArrayList<LightField> fields = new java.util.ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                fields.add(new LightField(
                        buffer.readUUID(), buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarLong()
                ));
            }
            return new LightFieldSnapshotPacket(fields);
        }
    }
}
