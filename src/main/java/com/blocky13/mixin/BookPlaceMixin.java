package com.blocky13.mixin;

import com.blocky13.BookPileBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Using a plain book on a block lays it on the floor as a {@link BookPileBlock}.
 *
 * {@code Items.BOOK} is a plain {@link Item} with no {@code useOn}, so this hooks the base
 * method. {@code useOn} only runs after the clicked block's own interaction passed, so books
 * still go into chiseled bookshelves and enchanting tables still open as usual.
 */
@Mixin(Item.class)
public abstract class BookPlaceMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void blocky13$placeBookPile(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this != Items.BOOK) return;
        InteractionResult result = BookPileBlock.tryPlace(ctx);
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
}
