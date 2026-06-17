package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DyeBrushItem extends Item {

    // Each inner array holds 16 blocks in DyeColor.values() ordinal order.
    // MC 26.2 replaced the per-color vanilla blocks (Blocks.WHITE_WOOL, ...) with
    // ColorCollections (Blocks.WOOL, ...); family() unpacks one into the Block[]
    // this class expects, indexed by DyeColor ordinal.
    private static final Block[][] VANILLA_FAMILIES = {
        family(Blocks.WOOL),
        family(Blocks.CARPET),
        family(Blocks.CONCRETE),
        family(Blocks.CONCRETE_POWDER),
        family(Blocks.DYED_TERRACOTTA),
        family(Blocks.STAINED_GLASS),
        family(Blocks.STAINED_GLASS_PANE),
        family(Blocks.DYED_SHULKER_BOX),
    };

    /** Unpack a vanilla {@link ColorCollection} into a Block[] indexed by DyeColor ordinal. */
    private static Block[] family(ColorCollection<Block> coll) {
        DyeColor[] colors = DyeColor.values();
        Block[] arr = new Block[colors.length];
        for (int i = 0; i < colors.length; i++) {
            arr[i] = coll.pick(colors[i]);
        }
        return arr;
    }

    private static final Map<Block, Block[]> FAMILY_MAP = new HashMap<>();

    static {
        for (Block[] family : VANILLA_FAMILIES) {
            for (Block b : family) FAMILY_MAP.put(b, family);
        }
    }

    public DyeBrushItem(Properties properties) {
        super(properties);
    }

    /** Register a mod-added family of 16 dyeable blocks (in DyeColor.values() order). */
    public static void registerFamily(Block[] family) {
        for (Block b : family) FAMILY_MAP.put(b, family);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = ctx.getItemInHand();
        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);
        if (dyedColor == null) return InteractionResult.PASS;

        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block[] family = FAMILY_MAP.get(state.getBlock());
        if (family == null) return InteractionResult.PASS;

        int colorIndex = nearestDyeColorIndex(dyedColor.rgb());
        Block newBlock = family[colorIndex];
        if (newBlock == state.getBlock()) return InteractionResult.CONSUME;

        if (!level.isClientSide()) {
            BlockState newState = newBlock.defaultBlockState();
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                    && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                newState = newState.setValue(BlockStateProperties.WATERLOGGED,
                        state.getValue(BlockStateProperties.WATERLOGGED));
            }
            level.setBlock(pos, newState, 3);
        }
        return InteractionResult.SUCCESS;
    }

    /** Returns the index (0-15) in DyeColor.values() closest to the given RGB. */
    private static int nearestDyeColorIndex(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        DyeColor[] colors = DyeColor.values();
        for (int i = 0; i < colors.length; i++) {
            int tc = colors[i].getTextureDiffuseColor();
            double dr = r / 255.0 - ((tc >> 16) & 0xFF) / 255.0;
            double dg = g / 255.0 - ((tc >> 8) & 0xFF) / 255.0;
            double db = b / 255.0 - (tc & 0xFF) / 255.0;
            double dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

    /**
     * Mix an existing RGB (or -1 for none) with a list of dye colors.
     * Uses the leather-armor algorithm: average R/G/B and max-component, then
     * scale so the averaged max-component matches the averaged max-of-avg.
     */
    public static int mixColors(int existingRgb, List<DyeColor> dyes) {
        int totalR = 0, totalG = 0, totalB = 0, totalMax = 0, count = 0;

        if (existingRgb >= 0) {
            int er = (existingRgb >> 16) & 0xFF;
            int eg = (existingRgb >> 8) & 0xFF;
            int eb = existingRgb & 0xFF;
            totalR += er;
            totalG += eg;
            totalB += eb;
            totalMax += Math.max(er, Math.max(eg, eb));
            count++;
        }

        for (DyeColor dye : dyes) {
            int tc = dye.getTextureDiffuseColor();
            int dr = (tc >> 16) & 0xFF;
            int dg = (tc >> 8) & 0xFF;
            int db = tc & 0xFF;
            totalR += dr;
            totalG += dg;
            totalB += db;
            totalMax += Math.max(dr, Math.max(dg, db));
            count++;
        }

        if (count == 0) return 0xFFFFFF;

        int avgR = totalR / count;
        int avgG = totalG / count;
        int avgB = totalB / count;
        int avgMax = totalMax / count;
        int maxOfAvg = Math.max(avgR, Math.max(avgG, avgB));

        if (maxOfAvg == 0) return 0;

        int finalR = avgR * avgMax / maxOfAvg;
        int finalG = avgG * avgMax / maxOfAvg;
        int finalB = avgB * avgMax / maxOfAvg;

        return (Math.min(255, finalR) << 16) | (Math.min(255, finalG) << 8) | Math.min(255, finalB);
    }
}
