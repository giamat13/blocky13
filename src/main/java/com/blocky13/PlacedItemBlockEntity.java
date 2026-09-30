package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Holds the items of a {@link PlacedItemBlock}, bottom first, each with the yaw (degrees) it
 * was laid down at. Slots are always packed from index 0; the top item is the last non-empty one.
 */
public class PlacedItemBlockEntity extends BlockEntity {

    private final NonNullList<ItemStack> items = NonNullList.withSize(PlacedItemBlock.MAX_ITEMS, ItemStack.EMPTY);
    private final int[] yaws = new int[PlacedItemBlock.MAX_ITEMS];

    public PlacedItemBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PLACED_ITEM_ENTITY, pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getYaw(int slot) {
        return yaws[slot];
    }

    public int count() {
        int n = 0;
        while (n < items.size() && !items.get(n).isEmpty()) {
            n++;
        }
        return n;
    }

    /** Lays a single item on top of the stack. Returns false when the stack is full. */
    public boolean add(ItemStack stack, int yaw) {
        int n = count();
        if (n >= items.size()) {
            return false;
        }
        items.set(n, stack.copyWithCount(1));
        yaws[n] = yaw;
        sync();
        return true;
    }

    /** Takes the top item off the stack (empty if there is none). */
    public ItemStack removeTop() {
        int n = count();
        if (n == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack top = items.set(n - 1, ItemStack.EMPTY);
        sync();
        return top;
    }

    public ItemStack getTop() {
        int n = count();
        return n == 0 ? ItemStack.EMPTY : items.get(n - 1);
    }

    /** Persist + push to clients (the renderer reads the items straight from this entity). */
    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, items);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items, true);
        out.putIntArray("Yaws", yaws);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items.clear();
        ContainerHelper.loadAllItems(in, items);
        in.getIntArray("Yaws").ifPresent(saved -> System.arraycopy(saved, 0, yaws, 0, Math.min(saved.length, yaws.length)));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
