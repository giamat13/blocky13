package com.blocky13;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(BLOCK_CRAFTING);
        });
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name);
    }
}
