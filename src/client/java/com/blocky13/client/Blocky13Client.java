package com.blocky13.client;

import com.blocky13.CombinedBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.gui.screens.MenuScreens;

public class Blocky13Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Minecraft 26.1+ assigns the chunk-section render layer automatically from each
		// sprite's transparency, so transparent blocks (bars, chains) no longer need an
		// explicit cutout mapping. Combined with noOcclusion() on those blocks, the gaps
		// render correctly without any client-side registration.

		// Issue #12: the Block Crafting station's GUI screen.
		MenuScreens.register(CombinedBlocks.BLOCK_CRAFTING_MENU, BlockCraftingScreen::new);

		// Issue #12/#20: wrap the combined block's/slab's baked model so each face (or half)
		// is textured from the block entity's render data (see CombinedBlockStateModel and
		// CombinedSlabStateModel).
		ModelLoadingPlugin.register(ctx ->
			ctx.modifyBlockModelAfterBake().register((model, context) -> {
				if (context.state().is(CombinedBlocks.COMBINED_BLOCK)) {
					return new CombinedBlockStateModel(model);
				}
				if (context.state().is(CombinedBlocks.COMBINED_SLAB)) {
					return new CombinedSlabStateModel(model);
				}
				return model;
			}));
	}
}
