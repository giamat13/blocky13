package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BannerPatternLayers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PatternedCarpetBlock extends BaseEntityBlock {

    public PatternedCarpetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PatternedCarpetBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PatternedCarpetBlockEntity be) {
            BannerPatternLayers layers = stack.getOrDefault(DataComponents.BANNER_PATTERNS,
                    BannerPatternLayers.EMPTY);
            be.setPatterns(layers);
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
