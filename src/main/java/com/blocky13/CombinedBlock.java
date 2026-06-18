package com.blocky13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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

    // --- Functional faces (issue #12): the top face's block decides how entities
    // landing on / walking over the block behave. Add a case here to support more. ---

    /** The block shown on a face (its plate, or the main block), or AIR if no entity. */
    private static Block faceBlock(BlockGetter level, BlockPos pos, Direction face) {
        return level.getBlockEntity(pos) instanceof CombinedBlockEntity be ? be.getFace(face) : Blocks.AIR;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        Block top = faceBlock(level, pos, Direction.UP);
        BlockState topState = top.defaultBlockState();
        double fall = Math.sqrt(Math.max(fallDistance, 0.0));
        if (top == Blocks.SLIME_BLOCK && !entity.isSuppressingBounce()) {
            // No fall damage + bounce; 0.4*sqrt(h) roughly conserves energy like a real slime block.
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
            bounce(entity, 0.4 * fall);
            return;
        }
        if (topState.is(BlockTags.BEDS) && !entity.isSuppressingBounce()) {
            entity.causeFallDamage(fallDistance, 0.5F, level.damageSources().fall()); // weaker than slime
            bounce(entity, 0.25 * fall);
            return;
        }
        if (top == Blocks.HONEY_BLOCK || top == Blocks.HAY_BLOCK || topState.is(BlockTags.WOOL)) {
            entity.causeFallDamage(fallDistance, 0.2F, level.damageSources().fall()); // soft landing
            return;
        }
        if (top == Blocks.POINTED_DRIPSTONE) {
            entity.causeFallDamage(fallDistance, 2.0F, level.damageSources().fall()); // spikes hurt more
            return;
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private static void bounce(Entity entity, double speed) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, Math.min(speed, 2.0), motion.z);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState onState, Entity entity) {
        Block top = faceBlock(level, pos, Direction.UP);
        if (top == Blocks.MAGMA_BLOCK) {
            if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) {
                entity.hurt(level.damageSources().hotFloor(), 1.0F);
            }
        } else if (top == Blocks.CACTUS) {
            if (entity instanceof LivingEntity) {
                entity.hurt(level.damageSources().cactus(), 1.0F);
            }
        } else if (top == Blocks.SLIME_BLOCK || top == Blocks.HONEY_BLOCK
                || top == Blocks.SOUL_SAND || top == Blocks.MUD) {
            // Sticky/slow walking, mirroring vanilla slime/honey/soul sand/mud.
            double absY = Math.abs(entity.getDeltaMovement().y);
            if (absY < 0.1 && !entity.isSteppingCarefully()) {
                double scale = 0.4 + absY * 0.2;
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(scale, 1.0, scale));
            }
        }
        super.stepOn(level, pos, onState, entity);
    }
}
