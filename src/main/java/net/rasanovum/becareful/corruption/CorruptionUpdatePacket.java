package net.rasanovum.becareful.corruption;

import net.minecraft.network.FriendlyByteBuf;
import net.rasanovum.rosetta.network.RosettaPacket;

public record CorruptionUpdatePacket(float value) implements RosettaPacket {
    public static void write(CorruptionUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.value());
    }

    public static CorruptionUpdatePacket read(FriendlyByteBuf buffer) {
        return new CorruptionUpdatePacket(buffer.readFloat());
    }
}
