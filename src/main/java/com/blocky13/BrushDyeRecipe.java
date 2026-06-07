package com.blocky13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BrushDyeRecipe extends CustomRecipe {

    public static final RecipeSerializer<BrushDyeRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(new BrushDyeRecipe()),
            StreamCodec.unit(new BrushDyeRecipe()));

    public BrushDyeRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasBrush = false;
        boolean hasDye = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.BRUSH) || stack.getItem() instanceof DyeBrushItem) {
                if (hasBrush) return false;
                hasBrush = true;
            } else if (stack.getItem() instanceof DyeItem) {
                hasDye = true;
            } else {
                return false;
            }
        }
        return hasBrush && hasDye;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack brushStack = ItemStack.EMPTY;
        List<DyeColor> dyes = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.BRUSH) || stack.getItem() instanceof DyeBrushItem) {
                brushStack = stack;
            } else if (stack.getItem() instanceof DyeItem) {
                DyeColor color = stack.get(DataComponents.DYE);
                if (color != null) dyes.add(color);
            }
        }

        if (brushStack.isEmpty() || dyes.isEmpty()) return ItemStack.EMPTY;

        int existingRgb = -1;
        if (brushStack.getItem() instanceof DyeBrushItem) {
            DyedItemColor existing = brushStack.get(DataComponents.DYED_COLOR);
            if (existing != null) existingRgb = existing.rgb();
        }

        int rgb = DyeBrushItem.mixColors(existingRgb, dyes);
        ItemStack result = new ItemStack(ModItems.DYE_BRUSH);
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
        return result;
    }

    @Override
    public RecipeSerializer<BrushDyeRecipe> getSerializer() {
        return SERIALIZER;
    }
}
