package net.rasanovum.becareful.light;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record LightField(UUID id, BlockPos center, int radius, long expiresAt) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<LightField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("id").forGetter(LightField::id),
            BlockPos.CODEC.fieldOf("center").forGetter(LightField::center),
            Codec.INT.fieldOf("radius").forGetter(LightField::radius),
            Codec.LONG.fieldOf("expires_at").forGetter(LightField::expiresAt)
    ).apply(instance, LightField::new));

    public boolean contains(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 fieldCenter = Vec3.atCenterOf(center);
        double distance = eye.distanceToSqr(fieldCenter);
        return distance <= (double) radius * radius;
    }
}
