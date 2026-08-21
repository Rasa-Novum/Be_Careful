package net.rasanovum.becareful.light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.rosetta.attachment.LevelAttachmentKey;
import net.rasanovum.rosetta.attachment.RosettaAttachments;

import java.util.List;
import java.util.UUID;

public final class LightFieldManager {
    private static final LevelAttachmentKey<LightFieldStore> FIELDS =
            RosettaAttachments.level(BeCareful.MOD_ID).persistent(
                    "light_fields", () -> new LightFieldStore(List.of()), LightFieldStore.CODEC
            );
    private static final DustParticleOptions DEBUG_PARTICLE =
            new DustParticleOptions(new Vector3f(1.0F, 1.0F, 0.0F), 1.0F);

    private LightFieldManager() {}

    /** Forces the persistent attachment key to be registered during loader bootstrap. */
    public static void bootstrap() {}

    public static LightField create(ServerLevel level, Player source) {
        LightField field = new LightField(
                UUID.randomUUID(),
                BlockPos.containing(source.getEyePosition()),
                Math.max(1, BeCarefulConfig.lightFieldRadius),
                level.getGameTime() + Math.max(1, BeCarefulConfig.lightFieldDurationTicks)
        );
        List<LightField> fields = FIELDS.getOrCreate(level).fields();
        fields.add(field);
        FIELDS.markDirty(level);
        LightFieldNetworking.sync(level);
        return field;
    }

    public static void tick(ServerLevel level) {
        List<LightField> fields = FIELDS.getOrCreate(level).fields();
        long gameTime = level.getGameTime();
        boolean changed = fields.removeIf(field -> field.expiresAt() <= gameTime);
        if (changed) {
            FIELDS.markDirty(level);
            LightFieldNetworking.sync(level);
        }

        if (BeCarefulConfig.lightFieldDebugParticles) {
            for (LightField field : fields) {
                spawnBoundaryParticles(level, field);
            }
        }
    }

    public static boolean contains(ServerLevel level, Player player) {
        long gameTime = level.getGameTime();
        for (LightField field : FIELDS.getOrCreate(level).fields()) {
            if (field.expiresAt() > gameTime && field.contains(player)) {
                return true;
            }
        }
        return false;
    }

    public static List<LightField> activeFields(ServerLevel level) {
        long gameTime = level.getGameTime();
        return FIELDS.getOrCreate(level).fields().stream()
                .filter(field -> field.expiresAt() > gameTime)
                .toList();
    }

    public static List<ServerPlayer> playersInside(ServerLevel level, LightField field) {
        return level.players().stream()
                .filter(field::contains)
                .toList();
    }

    private static void spawnBoundaryParticles(ServerLevel level, LightField field) {
        RandomSource random = level.random;
        Vec3 center = Vec3.atCenterOf(field.center());
        int count = 8;
        for (int i = 0; i < count; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double z = random.nextDouble() * 2.0 - 1.0;
            double radial = Math.sqrt(Math.max(0.0, 1.0 - z * z));
            double distance = field.radius() + 0.15;
            double x = center.x + radial * Math.cos(theta) * distance;
            double y = center.y + z * distance;
            double particleZ = center.z + radial * Math.sin(theta) * distance;
            level.sendParticles(DEBUG_PARTICLE, x, y, particleZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
