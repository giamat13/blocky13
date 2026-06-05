package com.blocky13;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final SlabBlock DIRT_SLAB = registerBlock("dirt_slab",
            new SlabBlock(props("dirt_slab", Blocks.DIRT)));

    public static final SlabBlock IRON_BLOCK_SLAB = registerBlock("iron_block_slab",
            new SlabBlock(props("iron_block_slab", Blocks.IRON_BLOCK)));

    public static final SlabBlock COAL_BLOCK_SLAB = registerBlock("coal_block_slab",
            new SlabBlock(props("coal_block_slab", Blocks.COAL_BLOCK)));

    public static final SlabBlock COPPER_BLOCK_SLAB = registerBlock("copper_block_slab",
            new SlabBlock(props("copper_block_slab", Blocks.COPPER_BLOCK)));

    public static final SlabBlock GOLD_BLOCK_SLAB = registerBlock("gold_block_slab",
            new SlabBlock(props("gold_block_slab", Blocks.GOLD_BLOCK)));

    public static final SlabBlock REDSTONE_BLOCK_SLAB = registerBlock("redstone_block_slab",
            new SlabBlock(props("redstone_block_slab", Blocks.REDSTONE_BLOCK)));

    public static final SlabBlock EMERALD_BLOCK_SLAB = registerBlock("emerald_block_slab",
            new SlabBlock(props("emerald_block_slab", Blocks.EMERALD_BLOCK)));

    public static final SlabBlock LAPIS_BLOCK_SLAB = registerBlock("lapis_block_slab",
            new SlabBlock(props("lapis_block_slab", Blocks.LAPIS_BLOCK)));

    public static final SlabBlock DIAMOND_BLOCK_SLAB = registerBlock("diamond_block_slab",
            new SlabBlock(props("diamond_block_slab", Blocks.DIAMOND_BLOCK)));

    public static final SlabBlock NETHERITE_BLOCK_SLAB = registerBlock("netherite_block_slab",
            new SlabBlock(props("netherite_block_slab", Blocks.NETHERITE_BLOCK)));

    private static BlockBehaviour.Properties props(String name, Block copyFrom) {
        return BlockBehaviour.Properties.ofFullCopy(copyFrom)
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name)));
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK,
                Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name),
                new BlockItem(block, new Item.Properties().setId(
                        ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name)))));
    }

    public static void registerModBlocks() {
        Blocky13.LOGGER.info("Registering Mod Blocks for " + Blocky13.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            entries.accept(DIRT_SLAB);
            entries.accept(IRON_BLOCK_SLAB);
            entries.accept(COAL_BLOCK_SLAB);
            entries.accept(COPPER_BLOCK_SLAB);
            entries.accept(GOLD_BLOCK_SLAB);
            entries.accept(REDSTONE_BLOCK_SLAB);
            entries.accept(EMERALD_BLOCK_SLAB);
            entries.accept(LAPIS_BLOCK_SLAB);
            entries.accept(DIAMOND_BLOCK_SLAB);
            entries.accept(NETHERITE_BLOCK_SLAB);
        });
    }
}
