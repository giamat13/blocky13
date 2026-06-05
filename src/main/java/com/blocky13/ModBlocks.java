package com.blocky13;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModBlocks {
    /** Every mod block, in registration order -> Building Blocks tab. */
    private static final List<Block> BUILDING_ORDER = new ArrayList<>();
    /** Blocks made of redstone -> also shown in the Redstone Blocks tab. */
    private static final List<Block> REDSTONE_ORDER = new ArrayList<>();
    /** Blocks that must render on the cutout layer (transparent gaps). Read by the client initializer. */
    public static final List<Block> CUTOUT_BLOCKS = new ArrayList<>();

    /** The base whose variants act as redstone power sources and appear in the Redstone tab. */
    private static final String REDSTONE_BASE = "redstone_block";

    /** Base blocks (id prefix -> vanilla block to copy properties from). */
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
            boolean rs = base.equals(REDSTONE_BASE);
            registerSlab(base + "_slab", copyFrom, rs);
            registerStairs(base + "_stairs", copyFrom, rs);
            registerFence(base + "_fence", copyFrom, rs);
            registerFenceGate(base + "_fence_gate", copyFrom, rs);
            registerDoor(base + "_door", copyFrom, rs);
            registerTrapdoor(base + "_trapdoor", copyFrom, rs);
            registerPressurePlate(base + "_pressure_plate", copyFrom, rs);
            registerButton(base + "_button", copyFrom, rs);
            registerChain(base + "_chain", copyFrom, rs);
            registerBars(base + "_bars", copyFrom, rs);
        }

        // All blocks live in Building Blocks; redstone-material variants also appear in Redstone Blocks.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            for (Block block : BUILDING_ORDER) {
                entries.accept(block);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
            for (Block block : REDSTONE_ORDER) {
                entries.accept(block);
            }
        });
    }

    private static void registerSlab(String name, Block copyFrom, boolean rs) {
        register(name, rs ? new PoweredSlab(props(name, copyFrom)) : new SlabBlock(props(name, copyFrom)), rs);
    }

    private static void registerStairs(String name, Block copyFrom, boolean rs) {
        BlockState base = copyFrom.defaultBlockState();
        register(name, rs ? new PoweredStairs(base, props(name, copyFrom)) : new StairBlock(base, props(name, copyFrom)), rs);
    }

    private static void registerFence(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredFence(p) : new FenceBlock(p), rs);
    }

    private static void registerFenceGate(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredFenceGate(WoodType.OAK, p) : new FenceGateBlock(WoodType.OAK, p), rs);
    }

    private static void registerDoor(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredDoor(BlockSetType.OAK, p) : new DoorBlock(BlockSetType.OAK, p), rs);
    }

    private static void registerTrapdoor(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredTrapdoor(BlockSetType.OAK, p) : new TrapDoorBlock(BlockSetType.OAK, p), rs);
    }

    // Pressure plates and buttons are already redstone components (emit power when activated); no powered override.
    private static void registerPressurePlate(String name, Block copyFrom, boolean rs) {
        register(name, new PressurePlateBlock(BlockSetType.IRON, props(name, copyFrom).noOcclusion()), rs);
    }

    private static void registerButton(String name, Block copyFrom, boolean rs) {
        register(name, new ButtonBlock(BlockSetType.IRON, 20, props(name, copyFrom).noOcclusion()), rs);
    }

    private static void registerChain(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        Block b = register(name, rs ? new PoweredChain(p) : new ChainBlock(p), rs);
        CUTOUT_BLOCKS.add(b);
    }

    private static void registerBars(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        Block b = register(name, rs ? new PoweredBars(p) : new IronBarsBlock(p), rs);
        CUTOUT_BLOCKS.add(b);
    }

    private static BlockBehaviour.Properties props(String name, Block copyFrom) {
        return BlockBehaviour.Properties.ofFullCopy(copyFrom)
                .setId(ResourceKey.create(Registries.BLOCK, id(name)));
    }

    private static <T extends Block> T register(String name, T block, boolean redstoneGroup) {
        registerBlockItem(name, block);
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        BUILDING_ORDER.add(block);
        if (redstoneGroup) {
            REDSTONE_ORDER.add(block);
        }
        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, id(name),
                new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name);
    }

    // ---- Redstone-powered variants: constant signal source of strength 15, like a block of redstone. ----

    private static class PoweredSlab extends SlabBlock {
        PoweredSlab(Properties p) { super(p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredStairs extends StairBlock {
        PoweredStairs(BlockState base, Properties p) { super(base, p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredFence extends FenceBlock {
        PoweredFence(Properties p) { super(p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredFenceGate extends FenceGateBlock {
        PoweredFenceGate(WoodType w, Properties p) { super(w, p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredDoor extends DoorBlock {
        PoweredDoor(BlockSetType t, Properties p) { super(t, p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredTrapdoor extends TrapDoorBlock {
        PoweredTrapdoor(BlockSetType t, Properties p) { super(t, p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredChain extends ChainBlock {
        PoweredChain(Properties p) { super(p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredBars extends IronBarsBlock {
        PoweredBars(Properties p) { super(p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }
}
