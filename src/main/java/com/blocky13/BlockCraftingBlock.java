package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Block Crafting station (issue #12): right-clicking it opens a GUI where you drop
 * a block and take 16 thin plates of it. Opens on any non-sneaking use (like a chest),
 * so holding a block opens the menu rather than placing the block.
 */
public class BlockCraftingBlock extends Block {

    private static final Component CONTAINER_TITLE = Component.translatable("container.blocky13.block_crafting");

    public BlockCraftingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return open(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return open(level, pos, player);
    }

    private static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new BlockCraftingMenu(id, inv, ContainerLevelAccess.create(level, pos)),
                    CONTAINER_TITLE));
        }
        return InteractionResult.SUCCESS;
    }
}
