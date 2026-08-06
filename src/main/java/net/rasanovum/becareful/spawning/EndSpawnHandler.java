package net.rasanovum.becareful.spawning;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.util.AdvancementManager;

import java.util.Random;

public class EndSpawnHandler {

    public static final boolean END_FEATURES_ENABLED = BeCarefulConfig.doEndFeatures;

    public static InteractionResultHolder<ItemStack> onUseEnderEye(Player player, Level level, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.ENDER_EYE) && level.dimension() == Level.END && END_FEATURES_ENABLED) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            player.swing(hand);

            if (!level.isClientSide()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    AdvancementManager.award(serverPlayer, AdvancementManager.ENDER_EYE_USED_IN_END);
                }
                EyeOfEnder eyeOfEnder = new EyeOfEnder(level, player.getX(), player.getY(0.5D), player.getZ());

                BlockPos targetPos = new BlockPos(0, 75, 0);
                eyeOfEnder.signalTo(targetPos);

                level.addFreshEntity(eyeOfEnder);
                level.playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL,
                        0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    public static void onPlayerEnterEnd(ServerPlayer player, ServerLevel originWorld, ServerLevel targetWorld) {
        if (targetWorld.dimension() == Level.END && END_FEATURES_ENABLED) {
            BlockPos safeSpawn = findSafeEndIslandLocation(targetWorld);

            generateSafetyPlatform(targetWorld, safeSpawn);
            player.teleportTo(
                    targetWorld,
                    safeSpawn.getX() + 0.5D,
                    safeSpawn.getY() + 1.0D,
                    safeSpawn.getZ() + 0.5D,
                    player.getYRot(),
                    player.getXRot()
            );
        }
    }

    private static BlockPos findSafeEndIslandLocation(ServerLevel endWorld) {
        Random rand = new Random();

        int minRadius = BeCarefulConfig.minSpawnRadius;
        int maxRadius = BeCarefulConfig.maxSpawnRadius;
        int radiusRange = maxRadius - minRadius;

        for (int attempts = 0; attempts < 200; attempts++) {
            double angle = rand.nextDouble() * 2.0 * Math.PI;
            double distance = minRadius + (rand.nextDouble() * radiusRange);

            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            BlockPos checkPos = new BlockPos(x, 64, z);
            if (endWorld.getBiome(checkPos).is(Biomes.END_HIGHLANDS) ||
                    endWorld.getBiome(checkPos).is(Biomes.END_MIDLANDS)) {

                int surfaceY = endWorld.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                if (surfaceY > 30) {
                    return new BlockPos(x, surfaceY, z);
                }
            }
        }
        return new BlockPos(minRadius, 75, minRadius);
    }

    private static void generateSafetyPlatform(ServerLevel level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos target = center.offset(dx, -1, dz);
                if (level.getBlockState(target).isAir()) {
                    level.setBlockAndUpdate(target, Blocks.END_STONE.defaultBlockState());
                }
            }
        }
    }
}
