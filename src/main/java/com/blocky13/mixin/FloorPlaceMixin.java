package com.blocky13.mixin;

import com.blocky13.BookPileBlock;
import com.blocky13.PlacedItemBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lays items on the floor when they are used on a block: a plain book becomes a
 * {@link BookPileBlock}, and flat items (music discs, paper, maps, ...) become a
 * {@link PlacedItemBlock}.
 *
 * These are plain {@link Item}s (or, like maps, fall back to {@code super.useOn}), so this
 * hooks the base method. {@code useOn} only runs after the clicked block's own interaction
 * passed, so discs still go into jukeboxes and books into chiseled bookshelves.
 */
@Mixin(Item.class)
public abstract class FloorPlaceMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void blocky13$placeOnFloor(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = ctx.getItemInHand();
        InteractionResult result;
        if (stack.is(Items.BOOK)) {
            result = BookPileBlock.tryPlace(ctx);
        } else if (PlacedItemBlock.isPlaceable(stack, ctx.getPlayer())) {
            result = PlacedItemBlock.tryPlace(ctx);
        } else {
            return;
        }
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
}
