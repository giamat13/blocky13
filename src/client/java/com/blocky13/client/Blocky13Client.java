package com.blocky13.client;

import com.blocky13.ModBlocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

public class Blocky13Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Bars and chains have transparent gaps; render them on the cutout layer so you can see through them.
		for (Block block : ModBlocks.CUTOUT_BLOCKS) {
			BlockRenderLayerMap.putBlock(block, ChunkSectionLayer.CUTOUT);
		}
	}
}
