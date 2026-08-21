package net.rasanovum.becareful.light;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
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
    private LightFieldManager() {}

    /** Forces the persistent attachment key to be registered during loader bootstrap. */
    public static void bootstrap() {}

    public static LightField create(ServerLevel level, Player source) {
        BlockPos center = BlockPos.containing(source.getEyePosition());
        LightField field = new LightField(
                UUID.randomUUID(),
                center,
                Math.max(1, BeCarefulConfig.lightFieldRadius),
                level.getGameTime() + Math.max(1, BeCarefulConfig.lightFieldDurationTicks),
                placeLightSource(level, center)
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
        List<LightField> expired = fields.stream()
                .filter(field -> field.expiresAt() <= gameTime)
                .toList();
        boolean changed = fields.removeIf(field -> field.expiresAt() <= gameTime);
        for (LightField field : expired) {
            removeLightSourceIfUnused(level, field, fields);
        }
        if (changed) {
            FIELDS.markDirty(level);
            LightFieldNetworking.sync(level);
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

    private static boolean placeLightSource(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) return false;
        return level.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState());
    }

    private static void removeLightSourceIfUnused(ServerLevel level, LightField expired, List<LightField> remaining) {
        if (!expired.lightSourcePlaced()) return;
        for (int i = 0; i < remaining.size(); i++) {
            LightField field = remaining.get(i);
            if (field.center().equals(expired.center())) {
                remaining.set(i, new LightField(
                        field.id(), field.center(), field.radius(), field.expiresAt(), true
                ));
                return;
            }
        }
        if (level.getBlockState(expired.center()).is(Blocks.LIGHT)) {
            level.removeBlock(expired.center(), false);
        }
    }

}
