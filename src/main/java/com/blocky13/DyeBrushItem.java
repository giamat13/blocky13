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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DyeBrushItem extends Item {

    // Each inner array holds 16 blocks in DyeColor.values() ordinal order.
    private static final Block[][] VANILLA_FAMILIES = {
        // Wool
        { Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL,
          Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL,
          Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
          Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL },
        // Carpet
        { Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET,
          Blocks.YELLOW_CARPET, Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET,
          Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET,
          Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET },
        // Concrete
        { Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
          Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE,
          Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
          Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE },
        // Concrete Powder
        { Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER,
          Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER,
          Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER,
          Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER,
          Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER,
          Blocks.BLACK_CONCRETE_POWDER },
        // Stained Terracotta
        { Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
          Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA,
          Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA,
          Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA,
          Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA,
          Blocks.BLACK_TERRACOTTA },
        // Stained Glass
        { Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS,
          Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS,
          Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS,
          Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS,
          Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS,
          Blocks.BLACK_STAINED_GLASS },
        // Stained Glass Pane
        { Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS_PANE,
          Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
          Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
          Blocks.CYAN_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE,
          Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS_PANE,
          Blocks.BLACK_STAINED_GLASS_PANE },
        // Shulker Box
        { Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX,
          Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
          Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX,
          Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX,
          Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX,
          Blocks.BLACK_SHULKER_BOX },
    };

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

        if (!level.isClientSide) {
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
            float[] tc = colors[i].getTextureDiffuseColors();
            double dr = r / 255.0 - tc[0];
            double dg = g / 255.0 - tc[1];
            double db = b / 255.0 - tc[2];
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
            float[] tc = dye.getTextureDiffuseColors();
            int dr = (int) (tc[0] * 255);
            int dg = (int) (tc[1] * 255);
            int db = (int) (tc[2] * 255);
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
