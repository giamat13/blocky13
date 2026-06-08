package com.blocky13.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The loom screen draws a banner-flag preview by casting the banner-slot item to
 * {@link BannerItem}. Since we allow carpets in that slot, the cast would crash.
 * When the slot holds a non-banner (a carpet), report no result patterns so the
 * preview block is skipped; the rest of the screen renders normally.
 */
@Mixin(LoomScreen.class)
public class LoomScreenMixin {

    @ModifyExpressionValue(
        method = "extractBackground",
        at = @At(value = "FIELD",
                 target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;resultBannerPatterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
                 opcode = org.objectweb.asm.Opcodes.GETFIELD,
                 ordinal = 0))
    private BannerPatternLayers blocky13$skipPreviewForNonBanner(BannerPatternLayers original) {
        LoomMenu menu = ((LoomScreen) (Object) this).getMenu();
        ItemStack banner = menu.getBannerSlot().getItem();
        if (!banner.isEmpty() && !(banner.getItem() instanceof BannerItem)) {
            return null;
        }
        return original;
    }
}
