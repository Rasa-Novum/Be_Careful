package net.rasanovum.becareful.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.rasanovum.becareful.BeCarefulConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FrozenCampfireBlock extends CampfireBlock {

    private static final int AURA_RADIUS = BeCarefulConfig.frozenCampfireRadius;
    public static final Set<UUID> PLAYERS_NEAR_COLD_FIRE = new HashSet<>();

    public FrozenCampfireBlock(BlockBehaviour.Properties properties) {
        super(false, 2, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (!BeCarefulConfig.doFrozenFeatures) {
            level.scheduleTick(pos, this, 20);
            return;
        }

        AABB auraBox = new AABB(pos).inflate(AURA_RADIUS);
        java.util.List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, auraBox);

        for (Player player : nearbyPlayers) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
            PLAYERS_NEAR_COLD_FIRE.add(player.getUUID());
        }

        level.scheduleTick(pos, this, 20);
    }
}