package com.blocky13;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

    public static Item DYE_BRUSH;

    public static void register() {
        Identifier brushId = Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, "dye_brush");
        DYE_BRUSH = Registry.register(BuiltInRegistries.ITEM, brushId,
                new DyeBrushItem(
                        new Item.Properties()
                                .setId(ResourceKey.create(Registries.ITEM, brushId))
                                .stacksTo(1)
                ));

        Identifier recipeId = Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, "brush_dye");
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeId, BrushDyeRecipe.SERIALIZER);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(DYE_BRUSH);
        });
    }
}
