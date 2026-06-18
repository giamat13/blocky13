package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A thin plate of some block, produced at the Block Crafting station. Right-clicking
 * a face with it applies that block's texture to the face of a {@link CombinedBlock},
 * converting a plain full block into a combined block on first use.
 */
public class BlockPlateItem extends Item {

    public BlockPlateItem(Properties properties) {
        super(properties);
    }

    /** The block this plate represents, or {@code null} if it has not been set. */
    public static Block plateBlock(ItemStack stack) {
        return stack.get(CombinedBlocks.PLATE_BLOCK);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction face = ctx.getClickedFace();
        ItemStack stack = ctx.getItemInHand();
        Player player = ctx.getPlayer();

        Block plate = plateBlock(stack);
        if (plate == null) {
            return InteractionResult.PASS;
        }

        // Already a combined block: just retexture the clicked face.
        if (state.is(CombinedBlocks.COMBINED_BLOCK)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CombinedBlockEntity be) {
                be.setFace(face, plate);
                be.sync();
                consume(stack, player);
            }
            return InteractionResult.SUCCESS;
        }

        // Convert a plain full block into a combined block (its block becomes the main).
        if (canConvert(state, level, pos)) {
            if (!level.isClientSide()) {
                Block original = state.getBlock();
                level.setBlock(pos, CombinedBlocks.COMBINED_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                if (level.getBlockEntity(pos) instanceof CombinedBlockEntity be) {
                    be.setMainBlock(original);
                    be.setFace(face, plate);
                    be.sync();
                }
                consume(stack, player);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static boolean canConvert(BlockState state, Level level, BlockPos pos) {
        return !state.isAir()
                && !state.hasBlockEntity()
                && state.isCollisionShapeFullBlock(level, pos);
    }

    private static void consume(ItemStack stack, Player player) {
        if (player == null || !player.isCreative()) {
            stack.shrink(1);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Block plate = plateBlock(stack);
        if (plate != null) {
            return Component.translatable("item.blocky13.block_plate.of", plate.getName());
        }
        return super.getName(stack);
    }
}
