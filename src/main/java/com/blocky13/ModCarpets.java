package com.blocky13;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;

public class ModCarpets {

    public static final Map<DyeColor, PatternedCarpetBlock> CARPETS = new EnumMap<>(DyeColor.class);
    public static BlockEntityType<PatternedCarpetBlockEntity> CARPET_BLOCK_ENTITY_TYPE;

    public static void register() {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName() + "_patterned_carpet";
            PatternedCarpetBlock block = registerCarpet(name);
            CARPETS.put(color, block);
        }

        PatternedCarpetBlock[] allBlocks = CARPETS.values().toArray(new PatternedCarpetBlock[0]);
        Identifier betId = Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, "patterned_carpet");
        CARPET_BLOCK_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            betId,
            FabricBlockEntityTypeBuilder.create(PatternedCarpetBlockEntity::new, allBlocks).build()
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            for (Block b : CARPETS.values()) output.accept(b);
        });
    }

    private static PatternedCarpetBlock registerCarpet(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Blocky13.MOD_ID, name);
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CARPET)
                .setId(ResourceKey.create(Registries.BLOCK, id));
        PatternedCarpetBlock block = new PatternedCarpetBlock(props);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(BuiltInRegistries.ITEM, id,
                new BlockItem(block, new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)));
        return block;
    }

    public static PatternedCarpetBlock forColor(DyeColor color) {
        return CARPETS.get(color);
    }
}
