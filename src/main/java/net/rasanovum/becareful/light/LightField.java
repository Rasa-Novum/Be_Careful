package net.rasanovum.becareful.light;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record LightField(
        UUID id, Vec3 center, int radius, long startedAt, long expiresAt, boolean lightSourcePlaced,
        int contractionTicks, int reboundTicks
) {
    private static final float MIN_RADIUS = 0.15F;
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<LightField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("id").forGetter(LightField::id),
            Vec3.CODEC.fieldOf("center").forGetter(LightField::center),
            Codec.INT.fieldOf("radius").forGetter(LightField::radius),
            Codec.LONG.optionalFieldOf("started_at", -1L).forGetter(LightField::startedAt),
            Codec.LONG.fieldOf("expires_at").forGetter(LightField::expiresAt),
            Codec.BOOL.optionalFieldOf("light_source_placed", false).forGetter(LightField::lightSourcePlaced),
            Codec.INT.optionalFieldOf("contraction_ticks", 0).forGetter(LightField::contractionTicks),
            Codec.INT.optionalFieldOf("rebound_ticks", 0).forGetter(LightField::reboundTicks)
    ).apply(instance, LightField::new));

    public LightField(UUID id, Vec3 center, int radius, long startedAt, long expiresAt, boolean lightSourcePlaced) {
        this(id, center, radius, startedAt, expiresAt, lightSourcePlaced, 0, 0);
    }

    public boolean isDeathWave() {
        return contractionTicks > 0;
    }

    public FieldState stateAt(double gameTime) {
        if (isDeathWave()) {
            double age = Math.max(0.0, gameTime - startedAt);
            if (age < contractionTicks) {
                return new FieldState((float) (radius * (1.0 - age / contractionTicks)), 1.0F);
            }
            double expansion = age - contractionTicks;
            if (expansion <= reboundTicks) {
                return new FieldState((float) (radius * expansion / Math.max(1, reboundTicks)), 1.0F);
            }
            double fade = Math.max(1L, expiresAt - startedAt - contractionTicks - reboundTicks);
            return new FieldState(radius, (float) Math.max(0.0, 1.0 - (expansion - reboundTicks) / fade));
        }
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

    public boolean contains(Entity entity, long gameTime) {
        float currentRadius = stateAt(gameTime).radius();
        return entity.getBoundingBox().distanceToSqr(center) <= (double) currentRadius * currentRadius;
    }

    public record FieldState(float radius, float opacity) {}
}
