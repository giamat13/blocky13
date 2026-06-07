package com.blocky13.client;

import com.blocky13.ModCarpets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class Blocky13Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Minecraft 26.1+ assigns the chunk-section render layer automatically from each
		// sprite's transparency, so transparent blocks (bars, chains) no longer need an
		// explicit cutout mapping. Combined with noOcclusion() on those blocks, the gaps
		// render correctly without any client-side registration.

		BlockEntityRendererRegistry.register(
			ModCarpets.CARPET_BLOCK_ENTITY_TYPE,
			PatternedCarpetBlockEntityRenderer::new
		);
	}
}
