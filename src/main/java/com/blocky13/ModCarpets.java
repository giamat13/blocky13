package com.blocky13;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;

public class ModCarpets {

    public static final Map<DyeColor, Block> CARPETS = new EnumMap<>(DyeColor.class);

    public static void register() {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName() + "_patterned_carpet";
            Block block = registerCarpet(name);
            CARPETS.put(color, block);
        }

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            for (Block b : CARPETS.values()) output.accept(b);
        });
    }

    private static Block registerCarpet(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name);
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CARPET)
                .setId(ResourceKey.create(Registries.BLOCK, id));
        Block block = new Block(props);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(BuiltInRegistries.ITEM, id,
                new BlockItem(block, new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))));
        return block;
    }

    /** Returns the patterned carpet block for a given dye color, or null. */
    public static Block forColor(DyeColor color) {
        return CARPETS.get(color);
    }
}
