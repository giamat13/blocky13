package com.blocky13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Flat items (music discs, paper, maps, enchanted/written books, pottery sherds, ...) laid on
 * the floor, up to 4 stacked, each drawn lying flat by the client {@code PlacedItemRenderer}.
 *
 * Placed by using such an item on a block (see {@code FloorPlaceMixin}); using another one on
 * the stack adds it on top, and an empty-hand right-click takes the top item back. Which items
 * qualify: anything playable in a jukebox, plus the {@code blocky13:floor_placeable} item tag.
 */
public class PlacedItemBlock extends BaseEntityBlock {

    public static final MapCodec<PlacedItemBlock> CODEC = simpleCodec(PlacedItemBlock::new);
    public static final int MAX_ITEMS = 4;
    public static final IntegerProperty ITEMS = IntegerProperty.create("items", 1, MAX_ITEMS);
    public static final TagKey<Item> FLOOR_PLACEABLE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, "floor_placeable"));

    private static final VoxelShape[] SHAPES = new VoxelShape[MAX_ITEMS + 1];
    static {
        for (int n = 1; n <= MAX_ITEMS; n++) {
            SHAPES[n] = Block.box(3, 0, 3, 13, n, 13);
        }
    }

    public PlacedItemBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ITEMS, 1));
    }

    @Override
    protected MapCodec<PlacedItemBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ITEMS);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlacedItemBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // No block model; the items are drawn by the block-entity renderer.
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES[state.getValue(ITEMS)];
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
            return Blocks.AIR.defaultBlockState(); // the block entity drops its items on removal
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return level.getBlockEntity(pos) instanceof PlacedItemBlockEntity be ? be.getTop().copy() : ItemStack.EMPTY;
    }

    /** Using another placeable item on the stack puts it on top. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!isPlaceable(stack, player) || state.getValue(ITEMS) >= MAX_ITEMS) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            addItem(level, pos, player, stack);
        }
        return InteractionResult.SUCCESS;
    }

    /** An empty-hand right-click takes the top item back. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PlacedItemBlockEntity be) {
            ItemStack top = be.removeTop();
            if (!top.isEmpty()) {
                player.getInventory().placeItemBackInInventory(top);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            int left = be.count();
            if (left == 0) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(ITEMS, left), Block.UPDATE_ALL);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Items that can be laid on the floor. Items that already do something on right-click
     * (written and writable books open their GUI) only go down while sneaking.
     */
    public static boolean isPlaceable(ItemStack stack, Player player) {
        if (!stack.has(DataComponents.JUKEBOX_PLAYABLE) && !stack.is(FLOOR_PLACEABLE)) {
            return false;
        }
        boolean hasOwnUse = stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK);
        return !hasOwnUse || (player != null && player.isSecondaryUseActive());
    }

    /**
     * Lays the used item on the floor where it was used on a block (or on top of an existing
     * stack there). Called from {@code FloorPlaceMixin}; mutates the world server-side only.
     */
    public static InteractionResult tryPlace(UseOnContext useCtx) {
        BlockPlaceContext ctx = new BlockPlaceContext(useCtx);
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        BlockState existing = level.getBlockState(pos);

        if (existing.is(ModBlocks.PLACED_ITEM)) {
            if (existing.getValue(ITEMS) >= MAX_ITEMS) {
                return InteractionResult.PASS;
            }
        } else {
            BlockState state = ModBlocks.PLACED_ITEM.defaultBlockState();
            CollisionContext collision = player == null ? CollisionContext.empty() : CollisionContext.placementContext(player);
            if (!ctx.canPlace() || !state.canSurvive(level, pos) || !level.isUnobstructed(state, pos, collision)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        }
        if (!level.isClientSide()) {
            addItem(level, pos, player, ctx.getItemInHand());
        }
        return InteractionResult.SUCCESS;
    }

    private static void addItem(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof PlacedItemBlockEntity be)) {
            return;
        }
        // Face the item the way the player looks, with a little jitter so a stack looks casual.
        float look = player == null ? 0.0F : player.getYRot();
        int yaw = Math.round(look) + level.getRandom().nextInt(31) - 15;
        if (!be.add(stack, yaw)) {
            return;
        }
        level.setBlock(pos, level.getBlockState(pos).setValue(ITEMS, be.count()), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
        stack.consume(1, player);
    }
}
