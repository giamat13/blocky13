package com.blocky13;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

/**
 * Issue #12: "Block Crafting" — a workbench that turns any block into 16 thin plates,
 * and a combined block that wears a different plate texture on each of its six faces
 * while keeping the mining behaviour of a chosen main block.
 *
 * Faces are cosmetic for now (the issue also mentions functional slime/honey faces;
 * deferred). The combined block is created in-world by right-clicking a full block
 * with a plate, so it has no item form.
 */
public final class CombinedBlocks {

    private CombinedBlocks() {}

    /** Stores which block a plate (or a combined-block face) represents. */
    public static DataComponentType<Block> PLATE_BLOCK;

    public static Block BLOCK_CRAFTING;
    public static CombinedBlock COMBINED_BLOCK;
    public static BlockPlateItem BLOCK_PLATE;
    public static BlockEntityType<CombinedBlockEntity> COMBINED_BLOCK_ENTITY;
    public static MenuType<BlockCraftingMenu> BLOCK_CRAFTING_MENU;

    // Issue #20: a double slab built from two different slabs (no item; built in-world).
    public static CombinedSlabBlock COMBINED_SLAB;
    public static BlockEntityType<CombinedSlabBlockEntity> COMBINED_SLAB_ENTITY;

    public static void register() {
        BLOCK_CRAFTING_MENU = Registry.register(BuiltInRegistries.MENU, id("block_crafting"),
                new MenuType<>(BlockCraftingMenu::new, FeatureFlags.VANILLA_SET));

        PLATE_BLOCK = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("plate_block"),
                DataComponentType.<Block>builder()
                        .persistent(BuiltInRegistries.BLOCK.byNameCodec())
                        .networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK))
                        .build());

        // Block Crafting station (grass block + crafting table).
        Identifier craftingId = id("block_crafting");
        BLOCK_CRAFTING = new BlockCraftingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                .setId(ResourceKey.create(Registries.BLOCK, craftingId)));
        Registry.register(BuiltInRegistries.BLOCK, craftingId, BLOCK_CRAFTING);
        Registry.register(BuiltInRegistries.ITEM, craftingId, new BlockItem(BLOCK_CRAFTING,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, craftingId))));

        // Combined block (no item — built in-world by applying plates).
        Identifier combinedId = id("combined_block");
        COMBINED_BLOCK = new CombinedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .noOcclusion()
                .setId(ResourceKey.create(Registries.BLOCK, combinedId)));
        Registry.register(BuiltInRegistries.BLOCK, combinedId, COMBINED_BLOCK);

        COMBINED_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("combined_block"),
                new BlockEntityType<>(CombinedBlockEntity::new, Set.of(COMBINED_BLOCK)));

        // Thin plate item.
        Identifier plateId = id("block_plate");
        BLOCK_PLATE = new BlockPlateItem(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, plateId)));
        Registry.register(BuiltInRegistries.ITEM, plateId, BLOCK_PLATE);

        // Issue #20: combined slab (no item — built in-world by placing two different slabs).
        Identifier combinedSlabId = id("combined_slab");
        COMBINED_SLAB = new CombinedSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .noOcclusion()
                .setId(ResourceKey.create(Registries.BLOCK, combinedSlabId)));
        Registry.register(BuiltInRegistries.BLOCK, combinedSlabId, COMBINED_SLAB);
        COMBINED_SLAB_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("combined_slab"),
                new BlockEntityType<>(CombinedSlabBlockEntity::new, Set.of(COMBINED_SLAB)));

        // Placing a slab onto a single slab of a different material merges them into a combined slab.
        UseBlockCallback.EVENT.register(CombinedBlocks::combineSlabs);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(BLOCK_CRAFTING);
        });
    }

    /**
     * Issue #20: intercepts placing a slab onto a single slab of a <em>different</em> material.
     * Mirrors vanilla's slab-merge geometry (only when aiming at the empty half) but, instead of
     * refusing the merge as vanilla does for mismatched slabs, builds a {@link CombinedSlabBlock}
     * whose halves keep the two slabs' textures. Same-material slabs fall through to vanilla.
     */
    private static InteractionResult combineSlabs(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !player.mayBuild()) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof SlabBlock heldSlab)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState existing = level.getBlockState(pos);
        if (!(existing.getBlock() instanceof SlabBlock existingSlab)) {
            return InteractionResult.PASS;
        }
        SlabType type = existing.getValue(SlabBlock.TYPE);
        if (type == SlabType.DOUBLE || existingSlab == heldSlab) {
            return InteractionResult.PASS; // already full, or same slab -> let vanilla make a normal double slab
        }

        // Only merge when clicking the empty half, exactly as vanilla SlabBlock#canBeReplaced does.
        Direction face = hit.getDirection();
        boolean above = hit.getLocation().y - pos.getY() > 0.5;
        boolean merge = type == SlabType.BOTTOM
                ? face == Direction.UP || (above && face.getAxis().isHorizontal())
                : face == Direction.DOWN || (!above && face.getAxis().isHorizontal());
        if (!merge) {
            return InteractionResult.PASS; // aiming elsewhere -> let vanilla place an adjacent slab
        }

        // The existing slab keeps its half; the held slab fills the other.
        Block bottom = type == SlabType.BOTTOM ? existingSlab : heldSlab;
        Block top = type == SlabType.BOTTOM ? heldSlab : existingSlab;

        if (!level.isClientSide()) {
            level.setBlock(pos, COMBINED_SLAB.defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof CombinedSlabBlockEntity be) {
                be.setBlocks(bottom, top);
                be.sync();
            }
            SoundType sound = heldSlab.defaultBlockState().getSoundType();
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                    (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.isCreative()) {
                held.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name);
    }
}
