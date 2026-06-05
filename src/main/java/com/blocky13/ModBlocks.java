package com.blocky13;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModBlocks {
    /** Blocks in the order they should appear in the creative tab. */
    private static final List<Block> CREATIVE_ORDER = new ArrayList<>();

    /**
     * Base blocks the mod adds slabs AND stairs for.
     * The slab/stairs id is "<base>_slab" / "<base>_stairs" and properties are copied from the vanilla block.
     */
    private static final Object[][] BASES = {
            {"dirt",            Blocks.DIRT},
            {"iron_block",      Blocks.IRON_BLOCK},
            {"coal_block",      Blocks.COAL_BLOCK},
            {"copper_block",    Blocks.COPPER_BLOCK},
            {"gold_block",      Blocks.GOLD_BLOCK},
            {"redstone_block",  Blocks.REDSTONE_BLOCK},
            {"emerald_block",   Blocks.EMERALD_BLOCK},
            {"lapis_block",     Blocks.LAPIS_BLOCK},
            {"diamond_block",   Blocks.DIAMOND_BLOCK},
            {"netherite_block", Blocks.NETHERITE_BLOCK},
            {"raw_iron_block",   Blocks.RAW_IRON_BLOCK},
            {"raw_copper_block", Blocks.RAW_COPPER_BLOCK},
            {"raw_gold_block",   Blocks.RAW_GOLD_BLOCK},
            {"quartz_block",    Blocks.QUARTZ_BLOCK},
            {"amethyst_block",  Blocks.AMETHYST_BLOCK},
    };

    public static void registerModBlocks() {
        Blocky13.LOGGER.info("Registering Mod Blocks for " + Blocky13.MOD_ID);

        for (Object[] entry : BASES) {
            String base = (String) entry[0];
            Block copyFrom = (Block) entry[1];
            registerSlab(base + "_slab", copyFrom);
            registerStairs(base + "_stairs", copyFrom);
            registerFence(base + "_fence", copyFrom);
            registerFenceGate(base + "_fence_gate", copyFrom);
            registerDoor(base + "_door", copyFrom);
            registerTrapdoor(base + "_trapdoor", copyFrom);
            registerPressurePlate(base + "_pressure_plate", copyFrom);
            registerButton(base + "_button", copyFrom);
            registerChain(base + "_chain", copyFrom);
            registerBars(base + "_bars", copyFrom);
        }

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            for (Block block : CREATIVE_ORDER) {
                entries.accept(block);
            }
        });
    }

    private static void registerSlab(String name, Block copyFrom) {
        register(name, new SlabBlock(props(name, copyFrom)));
    }

    private static void registerStairs(String name, Block copyFrom) {
        register(name, new StairBlock(copyFrom.defaultBlockState(), props(name, copyFrom)));
    }

    private static void registerFence(String name, Block copyFrom) {
        register(name, new FenceBlock(props(name, copyFrom).noOcclusion()));
    }

    private static void registerFenceGate(String name, Block copyFrom) {
        register(name, new FenceGateBlock(WoodType.OAK, props(name, copyFrom).noOcclusion()));
    }

    private static void registerDoor(String name, Block copyFrom) {
        register(name, new DoorBlock(BlockSetType.IRON, props(name, copyFrom).noOcclusion()));
    }

    private static void registerTrapdoor(String name, Block copyFrom) {
        register(name, new TrapDoorBlock(BlockSetType.IRON, props(name, copyFrom).noOcclusion()));
    }

    private static void registerPressurePlate(String name, Block copyFrom) {
        register(name, new PressurePlateBlock(BlockSetType.IRON, props(name, copyFrom).noOcclusion()));
    }

    private static void registerButton(String name, Block copyFrom) {
        register(name, new ButtonBlock(BlockSetType.IRON, 20, props(name, copyFrom).noOcclusion()));
    }

    private static void registerChain(String name, Block copyFrom) {
        register(name, new ChainBlock(props(name, copyFrom).noOcclusion()));
    }

    private static void registerBars(String name, Block copyFrom) {
        register(name, new IronBarsBlock(props(name, copyFrom).noOcclusion()));
    }

    private static BlockBehaviour.Properties props(String name, Block copyFrom) {
        return BlockBehaviour.Properties.ofFullCopy(copyFrom)
                .setId(ResourceKey.create(Registries.BLOCK, id(name)));
    }

    private static <T extends Block> T register(String name, T block) {
        registerBlockItem(name, block);
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        CREATIVE_ORDER.add(block);
        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, id(name),
                new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name);
    }
}
