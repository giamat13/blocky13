package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Issue #7: a stackable layer block (1–8 layers, like snow) made of an arbitrary
 * material. Behaves like a snow layer but never melts, so any base material can be
 * laid down in thin layers.
 */
public class MaterialLayerBlock extends SnowLayerBlock {

    public MaterialLayerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Material layers don't melt.
    }
}
