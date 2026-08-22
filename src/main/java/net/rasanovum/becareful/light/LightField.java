package net.rasanovum.becareful.light;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record LightField(
        UUID id, Vec3 center, int radius, long startedAt, long expiresAt, boolean lightSourcePlaced
) {
    private static final float MIN_RADIUS = 0.15F;
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<LightField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("id").forGetter(LightField::id),
            Vec3.CODEC.fieldOf("center").forGetter(LightField::center),
            Codec.INT.fieldOf("radius").forGetter(LightField::radius),
            Codec.LONG.optionalFieldOf("started_at", -1L).forGetter(LightField::startedAt),
            Codec.LONG.fieldOf("expires_at").forGetter(LightField::expiresAt),
            Codec.BOOL.optionalFieldOf("light_source_placed", false).forGetter(LightField::lightSourcePlaced)
    ).apply(instance, LightField::new));

    public FieldState stateAt(double gameTime) {
        if (startedAt < 0) {
            return new FieldState(radius, 1.0F);
        }
        double duration = Math.max(1L, expiresAt - startedAt);
        float progress = (float) Math.max(0.0, Math.min(1.0, (gameTime - startedAt) / duration));
        return new FieldState(
                Math.max(MIN_RADIUS, radius * progress),
                1.0F - progress
        );
    }

    public boolean contains(Player player, long gameTime) {
        Vec3 eye = player.getEyePosition();
        double distance = eye.distanceToSqr(center);
        float currentRadius = stateAt(gameTime).radius();
        return distance <= (double) currentRadius * currentRadius;
    }

    public record FieldState(float radius, float opacity) {}
}
