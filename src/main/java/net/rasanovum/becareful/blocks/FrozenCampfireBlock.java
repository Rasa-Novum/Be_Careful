package net.rasanovum.becareful.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
/*? if >=1.21 {*/
import net.minecraft.world.ItemInteractionResult;
/*?}*/
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FrozenCampfireBlock extends CampfireBlock {

    public static BlockEntityType<FrozenCampfireBlockEntity> FROZEN_CAMPFIRE_ENTITY_TYPE;
    private static final int AURA_RADIUS = BeCarefulConfig.frozenCampfireRadius;
    public static final Set<UUID> PLAYERS_NEAR_COLD_FIRE = new HashSet<>();

    public FrozenCampfireBlock(BlockBehaviour.Properties properties) {
        super(false, 2, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(SIGNAL_FIRE, false).setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(LIT, false);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FrozenCampfireBlockEntity frozenFire) {

                for (int i = 0; i < frozenFire.getContainerSize(); i++) {
                    if (frozenFire.getItem(i).getCount() > 0) {
                        frozenFire.setItem(i, ItemStack.EMPTY);
                        break;
                    }
                }

                Containers.dropContents(level, pos, frozenFire);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    /*? if <1.21 {*/
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
    *//*?} else {*/
    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    /*?}*/
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FrozenCampfireBlockEntity frozenFire) {
            if (FrozenCampfireBlockEntity.getFuelBurnTime(heldItem.getItem()) > 0) {
                if (!level.isClientSide()) {
                    if (frozenFire.placeFuel(heldItem)) {
                        if (!state.getValue(LIT)) {
                            level.setBlock(pos, state.setValue(LIT, true), 3);
                        }
                        /*? if <1.21 {*/
                        /*return InteractionResult.SUCCESS;
                        *//*?} else {*/
                        return ItemInteractionResult.SUCCESS;
                        /*?}*/
                    }
                } else {
                    /*? if <1.21 {*/
                    /*return InteractionResult.CONSUME;
                    *//*?} else {*/
                    return ItemInteractionResult.CONSUME;
                    /*?}*/
                }
            }
        }
        /*? if <1.21 {*/
        /*return InteractionResult.PASS;
        *//*?} else {*/
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        /*?}*/
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrozenCampfireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BeCareful.FROZEN_CAMPFIRE_ENTITY_TYPE, FrozenCampfireBlockEntity::serverTick);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (!BeCarefulConfig.doFrozenFeatures) {
            level.scheduleTick(pos, this, 20);
            return;
        }

        if (!state.getValue(LIT)) {
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
