package net.rasanovum.becareful.portals;

/*? if fabric {*/
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
/*?}*/
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.util.AdvancementManager;
import net.rasanovum.becareful.warden.WardenStunAccess;

public class AncientPortalHandler {

    public static final boolean DEEP_DARK_ENABLED = BeCarefulConfig.doDeepDarkFeatures;

    /*? if fabric {*/
    public static void registerEvents() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
                (world, attacker, killedEntity) -> onKilledOtherEntity(world, killedEntity));
        UseBlockCallback.EVENT.register(AncientPortalHandler::useBlock);
    }
    /*?}*/

    public static void onKilledOtherEntity(Level world, net.minecraft.world.entity.Entity killedEntity) {
        if (killedEntity instanceof Warden warden
                && !world.isClientSide()
                && DEEP_DARK_ENABLED
                && !((WardenStunAccess) warden).beCareful$isStunned()) {
            ItemEntity keyDrop = new ItemEntity(world, killedEntity.getX(), killedEntity.getY(), killedEntity.getZ(),
                    new ItemStack(BeCareful.LOST_KEY));
            world.addFreshEntity(keyDrop);
        }
    }

    public static InteractionResult useBlock(net.minecraft.world.entity.player.Player player, Level level,
                                              net.minecraft.world.InteractionHand hand,
                                              net.minecraft.world.phys.BlockHitResult hitResult) {
            ItemStack heldItem = player.getItemInHand(hand);
            BlockPos clickedPos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(clickedPos);

            if (heldItem.is(BeCareful.LOST_KEY) && state.is(Blocks.REINFORCED_DEEPSLATE) && DEEP_DARK_ENABLED) {

                if (!level.isClientSide()) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    if (ignitePortalGateway(serverLevel, clickedPos)) {
                        level.playSound(
                                null, clickedPos,
                                SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS,
                                1.0F, 1.0F
                        );
                        if (player instanceof ServerPlayer serverPlayer) {
                            AdvancementManager.award(serverPlayer, AdvancementManager.FOLLOW_ENDER_EYE);
                        }
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                    }
                }

                player.swing(hand);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
    }
    private static boolean ignitePortalGateway(ServerLevel level, BlockPos framePos) {
        BlockPos innerStart = findInnerPortalAir(level, framePos);
        if (innerStart == null || !DEEP_DARK_ENABLED) return false;

        int minX = innerStart.getX(), maxX = innerStart.getX();
        int minY = innerStart.getY(), maxY = innerStart.getY();
        int minZ = innerStart.getZ(), maxZ = innerStart.getZ();

        boolean isAxisX = level.getBlockState(framePos.east()).is(Blocks.REINFORCED_DEEPSLATE) ||
                level.getBlockState(framePos.west()).is(Blocks.REINFORCED_DEEPSLATE);

        while (!level.getBlockState(new BlockPos(innerStart.getX(), minY - 1, innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE) && minY > level.getMinBuildHeight()) { minY--; }
        while (!level.getBlockState(new BlockPos(innerStart.getX(), maxY + 1, innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE) && maxY < level.getMaxBuildHeight()) { maxY++; }

        if (isAxisX) {
            while (!level.getBlockState(new BlockPos(minX - 1, innerStart.getY(), innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE)) { minX--; }
            while (!level.getBlockState(new BlockPos(maxX + 1, innerStart.getY(), innerStart.getZ())).is(Blocks.REINFORCED_DEEPSLATE)) { maxX++; }
        } else {
            while (!level.getBlockState(new BlockPos(innerStart.getX(), innerStart.getY(), minZ - 1)).is(Blocks.REINFORCED_DEEPSLATE)) { minZ--; }
            while (!level.getBlockState(new BlockPos(innerStart.getX(), innerStart.getY(), maxZ + 1)).is(Blocks.REINFORCED_DEEPSLATE)) { maxZ++; }
        }

        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        boolean ignited = false;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    targetPos.set(x, y, z);
                    level.setBlock(targetPos, Blocks.END_PORTAL.defaultBlockState(), 3);
                    ignited = true;
                }
            }
        }
        return ignited;
    }

    private static BlockPos findInnerPortalAir(Level level, BlockPos clicked) {
        for (Direction dir : Direction.values()) {
            BlockPos offset = clicked.relative(dir);
            BlockState state = level.getBlockState(offset);
            if (state.isAir() || state.is(Blocks.SCULK_VEIN)) {
                return offset;
            }
        }
        return null;
    }
}
