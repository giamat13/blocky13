package com.blocky13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Issue #20: a full block built from two <em>different</em> slabs — one slab filling the
 * bottom half, another filling the top half. The client model textures each half with its
 * slab's sprite (rendered from the block entity's render data); mining behaviour is taken
 * from the bottom slab. Created in-world by placing a slab onto a single slab of a
 * different material (see {@link CombinedBlocks#register()}), so it has no item form.
 */
public class CombinedSlabBlock extends BaseEntityBlock {

    public static final MapCodec<CombinedSlabBlock> CODEC = simpleCodec(CombinedSlabBlock::new);

    public CombinedSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CombinedSlabBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CombinedSlabBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Rendered through the (wrapped) block model pipeline, not a block-entity renderer.
        return RenderShape.MODEL;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CombinedSlabBlockEntity be) {
            return be.getBottom().defaultBlockState().getDestroyProgress(player, level, pos);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()
                && level.getBlockEntity(pos) instanceof CombinedSlabBlockEntity be) {
            popResource(level, pos, new ItemStack(be.getBottom()));
            popResource(level, pos, new ItemStack(be.getTop()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
