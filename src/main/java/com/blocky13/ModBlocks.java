package com.blocky13;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
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
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModBlocks {

    /** Colored brick base blocks in DyeColor.values() ordinal order (16 entries). */
    public static final Block[] COLORED_BRICKS = new Block[16];
    /** Every mod block, in registration order -> Building Blocks tab. */
    private static final List<Block> BUILDING_ORDER = new ArrayList<>();
    /** Blocks made of redstone -> also shown in the Redstone Blocks tab. */
    private static final List<Block> REDSTONE_ORDER = new ArrayList<>();

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
            // Concrete (issue #4)
            {"white_concrete",        Blocks.WHITE_CONCRETE},
            {"orange_concrete",       Blocks.ORANGE_CONCRETE},
            {"magenta_concrete",      Blocks.MAGENTA_CONCRETE},
            {"light_blue_concrete",   Blocks.LIGHT_BLUE_CONCRETE},
            {"yellow_concrete",       Blocks.YELLOW_CONCRETE},
            {"lime_concrete",         Blocks.LIME_CONCRETE},
            {"pink_concrete",         Blocks.PINK_CONCRETE},
            {"gray_concrete",         Blocks.GRAY_CONCRETE},
            {"light_gray_concrete",   Blocks.LIGHT_GRAY_CONCRETE},
            {"cyan_concrete",         Blocks.CYAN_CONCRETE},
            {"purple_concrete",       Blocks.PURPLE_CONCRETE},
            {"blue_concrete",         Blocks.BLUE_CONCRETE},
            {"brown_concrete",        Blocks.BROWN_CONCRETE},
            {"green_concrete",        Blocks.GREEN_CONCRETE},
            {"red_concrete",          Blocks.RED_CONCRETE},
            {"black_concrete",        Blocks.BLACK_CONCRETE},
            // Terracotta (issue #4)
            {"terracotta",            Blocks.TERRACOTTA},
            {"white_terracotta",      Blocks.WHITE_TERRACOTTA},
            {"orange_terracotta",     Blocks.ORANGE_TERRACOTTA},
            {"magenta_terracotta",    Blocks.MAGENTA_TERRACOTTA},
            {"light_blue_terracotta", Blocks.LIGHT_BLUE_TERRACOTTA},
            {"yellow_terracotta",     Blocks.YELLOW_TERRACOTTA},
            {"lime_terracotta",       Blocks.LIME_TERRACOTTA},
            {"pink_terracotta",       Blocks.PINK_TERRACOTTA},
            {"gray_terracotta",       Blocks.GRAY_TERRACOTTA},
            {"light_gray_terracotta", Blocks.LIGHT_GRAY_TERRACOTTA},
            {"cyan_terracotta",       Blocks.CYAN_TERRACOTTA},
            {"purple_terracotta",     Blocks.PURPLE_TERRACOTTA},
            {"blue_terracotta",       Blocks.BLUE_TERRACOTTA},
            {"brown_terracotta",      Blocks.BROWN_TERRACOTTA},
            {"green_terracotta",      Blocks.GREEN_TERRACOTTA},
            {"red_terracotta",        Blocks.RED_TERRACOTTA},
            {"black_terracotta",      Blocks.BLACK_TERRACOTTA},
            // Glass (issue #4)
            {"glass",                    Blocks.GLASS},
            {"white_stained_glass",      Blocks.WHITE_STAINED_GLASS},
            {"orange_stained_glass",     Blocks.ORANGE_STAINED_GLASS},
            {"magenta_stained_glass",    Blocks.MAGENTA_STAINED_GLASS},
            {"light_blue_stained_glass", Blocks.LIGHT_BLUE_STAINED_GLASS},
            {"yellow_stained_glass",     Blocks.YELLOW_STAINED_GLASS},
            {"lime_stained_glass",       Blocks.LIME_STAINED_GLASS},
            {"pink_stained_glass",       Blocks.PINK_STAINED_GLASS},
            {"gray_stained_glass",       Blocks.GRAY_STAINED_GLASS},
            {"light_gray_stained_glass", Blocks.LIGHT_GRAY_STAINED_GLASS},
            {"cyan_stained_glass",       Blocks.CYAN_STAINED_GLASS},
            {"purple_stained_glass",     Blocks.PURPLE_STAINED_GLASS},
            {"blue_stained_glass",       Blocks.BLUE_STAINED_GLASS},
            {"brown_stained_glass",      Blocks.BROWN_STAINED_GLASS},
            {"green_stained_glass",      Blocks.GREEN_STAINED_GLASS},
            {"red_stained_glass",        Blocks.RED_STAINED_GLASS},
            {"black_stained_glass",      Blocks.BLACK_STAINED_GLASS},
    };

    public static void registerModBlocks() {
        Blocky13.LOGGER.info("Registering Mod Blocks for " + Blocky13.MOD_ID);

        for (Object[] entry : BASES) {
            String base = (String) entry[0];
            Block copyFrom = (Block) entry[1];
            boolean rs = base.equals(REDSTONE_BASE);
            registerVariants(base, copyFrom, rs);
        }

        registerSandLayer();
        registerColoredBricks();

        // All blocks live in Building Blocks; redstone-material variants also appear in Redstone Blocks.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            for (Block block : BUILDING_ORDER) {
                output.accept(block);
            }
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            for (Block block : REDSTONE_ORDER) {
                output.accept(block);
            }
        });
    }

    private static void registerColoredBricks() {
        DyeColor[] colors = DyeColor.values();
        for (int i = 0; i < colors.length; i++) {
            String name = colors[i].getName() + "_bricks";
            Identifier id = id(name);
            BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                    .setId(ResourceKey.create(Registries.BLOCK, id));
            Block block = new Block(props);
            registerBlockItem(name, block);
            Registry.register(BuiltInRegistries.BLOCK, id, block);
            BUILDING_ORDER.add(block);
            COLORED_BRICKS[i] = block;
            // Full variant set (slab, stairs, fence, ... bars) for each colored brick.
            registerVariants(name, Blocks.BRICKS, false);
        }
        DyeBrushItem.registerFamily(COLORED_BRICKS);
    }

    /** Register the standard 10 variants for a base, copying properties from {@code copyFrom}. */
    private static void registerVariants(String base, Block copyFrom, boolean rs) {
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
        registerWall(base + "_wall", copyFrom, rs);
    }

    private static void registerSandLayer() {
        String name = "sand_layer";
        Identifier id = id(name);
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                .setId(ResourceKey.create(Registries.BLOCK, id));
        SandLayerBlock block = new SandLayerBlock(props);
        registerBlockItem(name, block);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        BUILDING_ORDER.add(block);
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
        register(name, rs ? new PoweredChain(p) : new ChainBlock(p), rs);
    }

    private static void registerBars(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredBars(p) : new IronBarsBlock(p), rs);
    }

    private static void registerWall(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredWall(p) : new WallBlock(p), rs);
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

    private static class PoweredWall extends WallBlock {
        PoweredWall(Properties p) { super(p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }
}
