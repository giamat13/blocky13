package com.blocky13.mixin;

import com.blocky13.ModCarpets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomMenu.class)
public class LoomMenuMixin {

    /**
     * Replace the loom's banner slot with one that also accepts (mod and vanilla) carpets,
     * so a carpet can be placed there and recolored like a banner.
     */
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"), require = 0)
    private void allowCarpetInBannerSlot(int containerId, Inventory inventory,
                                         ContainerLevelAccess access, CallbackInfo ci) {
        LoomMenu self = (LoomMenu) (Object) this;
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        Slot banner = self.getBannerSlot();

        Slot replacement = new Slot(banner.container, banner.getContainerSlot(), banner.x, banner.y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BannerItem || isRecolorableCarpet(stack);
            }
        };
        replacement.index = banner.index;
        menu.slots.set(banner.index, replacement);
    }

    /**
     * After the loom finishes its normal slot-change processing, check if a carpet was
     * placed in the banner slot. If so, produce a recolored patterned carpet in the result.
     */
    @Inject(method = "slotsChanged", at = @At("TAIL"), require = 0)
    private void handleCarpetRecolor(Container container, CallbackInfo ci) {
        LoomMenu self = (LoomMenu) (Object) this;

        ItemStack carpetStack = self.getBannerSlot().getItem();
        ItemStack dyeStack    = self.getDyeSlot().getItem();
        ItemStack resultStack = self.getResultSlot().getItem();

        // Only act when no normal banner result was produced
        if (!resultStack.isEmpty()) return;
        if (carpetStack.isEmpty() || dyeStack.isEmpty()) return;
        if (!isRecolorableCarpet(carpetStack)) return;

        if (!(dyeStack.getItem() instanceof DyeItem)) return;

        DyeColor targetColor = dyeStack.get(DataComponents.DYE);
        if (targetColor == null) return;
        Block targetCarpet = ModCarpets.forColor(targetColor);
        if (targetCarpet == null) return;

        self.getResultSlot().set(new ItemStack(targetCarpet));
    }

    private static boolean isRecolorableCarpet(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem bi)) return false;
        Block block = bi.getBlock();
        return block.defaultBlockState().is(BlockTags.WOOL_CARPETS)
                || ModCarpets.CARPETS.containsValue(block);
    }
}
