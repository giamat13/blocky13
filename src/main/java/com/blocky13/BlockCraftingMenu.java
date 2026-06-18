package com.blocky13;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Issue #12 GUI: a Stonecutter-style menu. Drop any block in the input slot and the
 * result slot fills with 16 thin plates of that block; taking them consumes one block.
 */
public class BlockCraftingMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    private static final int INV_START = 2;
    private static final int USE_ROW_END = 38;

    private final ContainerLevelAccess access;
    private final Container input = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            BlockCraftingMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();

    public BlockCraftingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public BlockCraftingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(CombinedBlocks.BLOCK_CRAFTING_MENU, containerId);
        this.access = access;

        this.addSlot(new Slot(this.input, 0, 20, 33));
        this.addSlot(new Slot(this.result, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack taken) {
                BlockCraftingMenu.this.input.removeItem(0, 1);
                BlockCraftingMenu.this.updateResult();
                super.onTake(player, taken);
            }
        });
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    private static boolean isPlatable(Block block) {
        return block != Blocks.AIR && !(block instanceof CombinedBlock) && !(block instanceof BlockCraftingBlock);
    }

    private void updateResult() {
        ItemStack in = this.input.getItem(0);
        ItemStack out = ItemStack.EMPTY;
        if (in.getItem() instanceof BlockItem blockItem && isPlatable(blockItem.getBlock())) {
            out = new ItemStack(CombinedBlocks.BLOCK_PLATE, 16);
            out.set(CombinedBlocks.PLATE_BLOCK, blockItem.getBlock());
        }
        this.result.setItem(0, out);
        this.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.input) {
            this.updateResult();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, INV_START, USE_ROW_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, moved);
            } else if (index == INPUT_SLOT) {
                if (!this.moveItemStackTo(stack, INV_START, USE_ROW_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                // not movable into the input; shuffle between inventory and hotbar
                if (index < 29) {
                    if (!this.moveItemStackTo(stack, 29, USE_ROW_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, INV_START, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == moved.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
            this.broadcastChanges();
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, CombinedBlocks.BLOCK_CRAFTING);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.input));
    }
}
