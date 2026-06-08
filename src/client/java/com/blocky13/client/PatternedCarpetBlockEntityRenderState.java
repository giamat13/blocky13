package com.blocky13.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class PatternedCarpetBlockEntityRenderState extends BlockEntityRenderState {

    public BannerPatternLayers patterns = BannerPatternLayers.EMPTY;
    public DyeColor color = DyeColor.WHITE;
}
