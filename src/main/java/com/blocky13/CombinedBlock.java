package com.blocky13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A full block that wears a different plate texture on each face (rendered by the
 * client model from the block entity's render data) while taking its hardness and
 * mining tool from the stored main block. Created in-world via {@link BlockPlateItem}.
 */
public class CombinedBlock extends BaseEntityBlock {

    public static final MapCodec<CombinedBlock> CODEC = simpleCodec(CombinedBlock::new);

    public CombinedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CombinedBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CombinedBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Rendered through the (wrapped) block model pipeline, not a block-entity renderer.
        return RenderShape.MODEL;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CombinedBlockEntity be) {
            return be.getMainBlock().defaultBlockState().getDestroyProgress(player, level, pos);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()
                && level.getBlockEntity(pos) instanceof CombinedBlockEntity be) {
            popResource(level, pos, new ItemStack(be.getMainBlock()));
            for (Direction face : Direction.values()) {
                Block plate = be.getRawFace(face);
                if (plate != null) {
                    ItemStack stack = new ItemStack(CombinedBlocks.BLOCK_PLATE);
                    stack.set(CombinedBlocks.PLATE_BLOCK, plate);
                    popResource(level, pos, stack);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
