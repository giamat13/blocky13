package com.blocky13;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {

    /** Colored brick base blocks in DyeColor.values() ordinal order (16 entries). */
    public static final Block[] COLORED_BRICKS = new Block[16];
    /** Every mod block, in registration order -> Building Blocks tab. */
    private static final List<Block> BUILDING_ORDER = new ArrayList<>();
    /** Blocks made of redstone -> also shown in the Redstone Blocks tab. */
    private static final List<Block> REDSTONE_ORDER = new ArrayList<>();
    /** Colored torches and lamps (issue #9) -> Functional Blocks tab. */
    private static final List<Block> FUNCTIONAL_ORDER = new ArrayList<>();

    /** A pile of 1-4 books lying on the floor (no item; placed by using a book on a block). */
    public static BookPileBlock BOOK_PILE;

    /** The base whose variants act as redstone power sources and appear in the Redstone tab. */
    private static final String REDSTONE_BASE = "redstone_block";

    /**
     * Base blocks (id prefix -> vanilla block to copy properties from).
     *
     * MC 26.2 collapsed the per-color vanilla blocks into {@link ColorCollection}s
     * (e.g. {@code Blocks.CONCRETE.pick(DyeColor.WHITE)} replaces the old
     * {@code Blocks.WHITE_CONCRETE}), and copper became a
     * {@link net.minecraft.world.level.block.WeatheringCopperCollection}, so the
     * color families are now built programmatically in DyeColor order to match the
     * generated asset names ({@code white_concrete}, ... {@code black_stained_glass}).
     */
    private static final List<Object[]> BASES = buildBases();

    private static List<Object[]> buildBases() {
        List<Object[]> bases = new ArrayList<>();
        Object[][] singles = {
                {"dirt",            Blocks.DIRT},
                {"iron_block",      Blocks.IRON_BLOCK},
                {"coal_block",      Blocks.COAL_BLOCK},
                {"copper_block",    Blocks.COPPER_BLOCK.weathering().unaffected()},
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
        for (Object[] s : singles) {
            bases.add(s);
        }
        // Concrete (issue #4) — 16 colors
        for (DyeColor c : DyeColor.values()) {
            bases.add(new Object[]{c.getName() + "_concrete", Blocks.CONCRETE.pick(c)});
        }
        // Terracotta (issue #4) — plain + 16 colors
        bases.add(new Object[]{"terracotta", Blocks.TERRACOTTA});
        for (DyeColor c : DyeColor.values()) {
            bases.add(new Object[]{c.getName() + "_terracotta", Blocks.DYED_TERRACOTTA.pick(c)});
        }
        // Glass (issue #4) — plain + 16 stained
        bases.add(new Object[]{"glass", Blocks.GLASS});
        for (DyeColor c : DyeColor.values()) {
            bases.add(new Object[]{c.getName() + "_stained_glass", Blocks.STAINED_GLASS.pick(c)});
        }
        return bases;
    }

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
        registerTorchesAndLamps();
        registerBookPile();

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
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            for (Block block : FUNCTIONAL_ORDER) {
                output.accept(block);
            }
        });
    }

    /** Issue #9: 16 colored torches (standing + wall, one item each) and 16 colored lamps. */
    private static void registerTorchesAndLamps() {
        for (DyeColor color : DyeColor.values()) {
            String cn = color.getName();

            // Torch: a standing block + a wall block sharing a single StandingAndWallBlockItem.
            Identifier torchId = id(cn + "_torch");
            Identifier wallTorchId = id(cn + "_wall_torch");
            ColoredTorchBlock torch = new ColoredTorchBlock(ParticleTypes.FLAME,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)
                            .setId(ResourceKey.create(Registries.BLOCK, torchId)));
            ColoredWallTorchBlock wallTorch = new ColoredWallTorchBlock(ParticleTypes.FLAME,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH)
                            .setId(ResourceKey.create(Registries.BLOCK, wallTorchId)));
            Registry.register(BuiltInRegistries.BLOCK, torchId, torch);
            Registry.register(BuiltInRegistries.BLOCK, wallTorchId, wallTorch);
            Registry.register(BuiltInRegistries.ITEM, torchId,
                    new StandingAndWallBlockItem(torch, wallTorch, Direction.DOWN,
                            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, torchId))));
            FUNCTIONAL_ORDER.add(torch);

            // Lamp: a full block that always glows (copies glowstone, light level 15).
            Identifier lampId = id(cn + "_lamp");
            Block lamp = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLOWSTONE)
                    .setId(ResourceKey.create(Registries.BLOCK, lampId)));
            registerBlockItem(cn + "_lamp", lamp);
            Registry.register(BuiltInRegistries.BLOCK, lampId, lamp);
            FUNCTIONAL_ORDER.add(lamp);
        }
    }

    /** Books placed on the floor. Has no item: the mixin on {@code Item#useOn} places it from a book. */
    private static void registerBookPile() {
        Identifier id = id("book_pile");
        BOOK_PILE = new BookPileBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(0.2F)
                .sound(SoundType.CHISELED_BOOKSHELF)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .setId(ResourceKey.create(Registries.BLOCK, id)));
        Registry.register(BuiltInRegistries.BLOCK, id, BOOK_PILE);
        FlammableBlockRegistry.getDefaultInstance().add(BOOK_PILE, 30, 60);
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

    /** Register the standard variants for a base, copying properties from {@code copyFrom}. */
    private static void registerVariants(String base, Block copyFrom, boolean rs) {
        registerSlab(base + "_slab", copyFrom, rs);
        registerVerticalSlab(base + "_vertical_slab", copyFrom, rs);
        registerLayer(base + "_layer", copyFrom, rs);
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

    private static void registerVerticalSlab(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredVerticalSlab(p) : new VerticalSlabBlock(p), rs);
    }

    private static void registerLayer(String name, Block copyFrom, boolean rs) {
        BlockBehaviour.Properties p = props(name, copyFrom).noOcclusion();
        register(name, rs ? new PoweredMaterialLayer(p) : new MaterialLayerBlock(p), rs);
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

    private static class PoweredVerticalSlab extends VerticalSlabBlock {
        PoweredVerticalSlab(Properties p) { super(p); }
        @Override protected boolean isSignalSource(BlockState s) { return true; }
        @Override protected int getSignal(BlockState s, BlockGetter l, BlockPos pos, Direction d) { return 15; }
    }

    private static class PoweredMaterialLayer extends MaterialLayerBlock {
        PoweredMaterialLayer(Properties p) { super(p); }
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
