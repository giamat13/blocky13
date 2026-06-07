package com.blocky13.mixin;

import com.blocky13.ModCarpets;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomMenu.class)
public class LoomMenuMixin {

    /** AbstractContainerMenu.slots - the list of all slots in this menu. */
    @Shadow @Final public NonNullList<Slot> slots;

    /**
     * After the loom finishes its normal slot-change processing, check if a carpet was
     * placed in the banner slot. If so, produce a recolored patterned carpet in the result.
     * Loom slot layout: 0 = banner/input, 1 = dye, 2 = pattern item, 3 = result.
     */
    @Inject(method = "slotsChanged", at = @At("TAIL"), require = 0)
    private void handleCarpetRecolor(Container container, CallbackInfo ci) {
        if (this.slots.size() < 4) return;

        ItemStack carpetStack = this.slots.get(0).getItem();
        ItemStack dyeStack    = this.slots.get(1).getItem();
        ItemStack resultStack = this.slots.get(3).getItem();

        // Only act when no normal banner result was produced
        if (!resultStack.isEmpty()) return;
        if (carpetStack.isEmpty() || dyeStack.isEmpty()) return;

        if (!(carpetStack.getItem() instanceof BlockItem bi)) return;

        Block block = bi.getBlock();
        boolean isCarpet = block.defaultBlockState().is(BlockTags.WOOL_CARPETS)
                || ModCarpets.CARPETS.containsValue(block);
        if (!isCarpet) return;

        if (!(dyeStack.getItem() instanceof DyeItem dyeItem)) return;

        DyeColor targetColor = dyeItem.getDyeColor();
        Block targetCarpet = ModCarpets.forColor(targetColor);
        if (targetCarpet == null) return;

        this.slots.get(3).set(new ItemStack(targetCarpet));
    }
}
