package net.rasanovum.becareful.corruption;

import net.minecraft.network.FriendlyByteBuf;
import net.rasanovum.rosetta.network.RosettaPacket;

public record CorruptionDebugPacket(int deepDarkTime, int warningRemaining, int dangerRemaining,
                                    boolean inDeepDark, boolean protectedByLight, boolean nearbySculk) implements RosettaPacket {
    public static void write(CorruptionDebugPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.deepDarkTime());
        buffer.writeVarInt(packet.warningRemaining());
        buffer.writeVarInt(packet.dangerRemaining());
        buffer.writeBoolean(packet.inDeepDark());
        buffer.writeBoolean(packet.protectedByLight());
        buffer.writeBoolean(packet.nearbySculk());
    }

    public static CorruptionDebugPacket read(FriendlyByteBuf buffer) {
        return new CorruptionDebugPacket(
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean()
        );
    }
}
