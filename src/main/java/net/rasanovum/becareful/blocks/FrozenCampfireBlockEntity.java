package net.rasanovum.becareful.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.becareful.BeCareful;

public class FrozenCampfireBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int burnTimeRemaining = 0;

    public FrozenCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(BeCareful.FROZEN_CAMPFIRE_ENTITY_TYPE, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FrozenCampfireBlockEntity blockEntity) {
        boolean isLit = state.getValue(CampfireBlock.LIT);
        boolean inventoryChanged = false;

        if (isLit && blockEntity.burnTimeRemaining > 0) {
            blockEntity.burnTimeRemaining--;
            inventoryChanged = true;

            if (blockEntity.burnTimeRemaining <= 0) {
                if (!blockEntity.consumeNextFuelItem()) {
                    level.setBlock(pos, state.setValue(CampfireBlock.LIT, false), 3);
                }
                inventoryChanged = true;
            }
        }

        if (inventoryChanged) {
            blockEntity.setChanged();
        }
    }

    private boolean consumeNextFuelItem() {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                int burnTicks = getFuelBurnTime(stack.getItem());
                if (burnTicks > 0) {
                    this.burnTimeRemaining += burnTicks;
                    this.items.set(i, ItemStack.EMPTY);
                    this.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean placeFuel(ItemStack fuelStack) {
        if (!this.getBlockState().getValue(CampfireBlock.LIT) && this.burnTimeRemaining <= 0) {
            int burnTicks = getFuelBurnTime(fuelStack.getItem());
            if (burnTicks > 0) {
                this.burnTimeRemaining = burnTicks;
                fuelStack.shrink(1);
                this.setChanged();
                return true;
            }
        }

        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                int burnTicks = getFuelBurnTime(fuelStack.getItem());
                if (burnTicks > 0) {
                    this.items.set(i, new ItemStack(fuelStack.getItem(), 1));
                    fuelStack.shrink(1);
                    this.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    // TODO make values configurable
    public static int getFuelBurnTime(Item item) {
        if (item == Items.SNOWBALL) return 200;
        if (item == Items.SNOW_BLOCK) return 600;
        if (item == Items.ICE) return 1200;
        if (item == Items.PACKED_ICE) return 2400;
        if (item == Items.BLUE_ICE) return 4800;
        return 0;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.items.clear();
        ContainerHelper.loadAllItems(nbt, this.items);
        this.burnTimeRemaining = nbt.getInt("BurnTimeRemaining");

        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, this.items);
        nbt.putInt("BurnTimeRemaining", this.burnTimeRemaining);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) this.load(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override public int getContainerSize() { return this.items.size(); }
    @Override public boolean isEmpty() { return this.items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return this.items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(this.items, slot, amount); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(this.items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { this.items.set(slot, stack); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { this.items.clear(); }
}