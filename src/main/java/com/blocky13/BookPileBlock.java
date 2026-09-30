package com.blocky13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A small pile of 1–4 books lying on the floor. Created in-world by using a vanilla book on a
 * block (see {@code BookPlaceMixin}); using another book on the pile adds one more, like
 * candles. Has no item of its own — it drops (and pick-blocks as) plain books.
 */
public class BookPileBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<BookPileBlock> CODEC = simpleCodec(BookPileBlock::new);
    public static final int MAX_BOOKS = 4;
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 1, MAX_BOOKS);

    /** Each book is 2px thick; the model's books all fit inside x/z 2..14. */
    private static final VoxelShape[] SHAPES = new VoxelShape[MAX_BOOKS + 1];
    static {
        for (int n = 1; n <= MAX_BOOKS; n++) {
            SHAPES[n] = Block.box(2, 0, 2, 14, n * 2, 14);
        }
    }

    public BookPileBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BOOKS, 1));
    }

    @Override
    protected MapCodec<BookPileBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BOOKS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (existing.is(this)) {
            return existing.setValue(BOOKS, Math.min(MAX_BOOKS, existing.getValue(BOOKS) + 1));
        }
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    /** Lets another book be added to the pile, the same way candles stack. */
    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        if (!ctx.isSecondaryUseActive() && ctx.getItemInHand().is(Items.BOOK) && state.getValue(BOOKS) < MAX_BOOKS) {
            return true;
        }
        return super.canBeReplaced(state, ctx);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES[state.getValue(BOOKS)];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState,
                                     RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(Items.BOOK);
    }

    /**
     * Places a book pile (or adds a book to one) where a book was used on a block. Mirrors
     * {@code BlockItem#place}, but the placed block is the pile rather than the book item's own block.
     */
    public static InteractionResult tryPlace(UseOnContext useCtx) {
        BlockPlaceContext ctx = new BlockPlaceContext(useCtx);
        if (!ctx.canPlace()) {
            return InteractionResult.PASS;
        }
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        BlockState state = ModBlocks.BOOK_PILE.getStateForPlacement(ctx);
        CollisionContext collision = player == null ? CollisionContext.empty() : CollisionContext.placementContext(player);
        if (state == null || !state.canSurvive(level, pos) || !level.isUnobstructed(state, pos, collision)) {
            return InteractionResult.PASS;
        }
        if (!level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return InteractionResult.FAIL;
        }
        level.playSound(player, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
        ctx.getItemInHand().consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
