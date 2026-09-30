# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew build       # builds build/libs/blocky13-1.0.0.jar
./gradlew runClient   # launches a dev Minecraft client
```

No test suite exists. Verification is done by running the client.

## Project Overview

Blocky13 is a Fabric mod for Minecraft 26.2 that adds decorative/functional block variants (slabs, vertical slabs, layers, stairs, fences, fence gates, doors, trapdoors, pressure plates, buttons, chains, bars, walls) for 15 base materials plus 16-color concrete/terracotta/glass and 16-color brick blocks, plus a sand layer block. Redstone-material variants emit a constant redstone signal of 15.

It also adds 16 colored torches (standing + wall) and 16 colored lamps (issue #9), and a `DyeBrushItem` that lets players recolor wool, terracotta, glass, and concrete blocks in-world using dyes.

Recently implemented issues: #7 (snow-style layer blocks for every base), #9 (colored torches & lamps), #12 (Block Crafting station + per-face "combined" blocks), #18 (vertical slabs), #19 (mobs cling to a honey block from below), #20 (a double slab of two *different* slabs).

**Issue #12 (`CombinedBlocks.java` + client `CombinedBlockStateModel.java`):**
- `BlockCraftingBlock` (the "Block Crafting" station, crafted from grass block + crafting table; distinct grass-topped-workbench texture from `create_block_crafting_textures`): any non-sneak right-click opens a real GUI — `BlockCraftingMenu` (server, Stonecutter-style: input slot 0 → result slot 1 with 16 `BlockPlateItem` plates of the input block, stored in the `PLATE_BLOCK` data component) + client `BlockCraftingScreen` (reuses the vanilla `stonecutter.png` background; only `extractBackground` is custom). Registered via `MenuType` in `CombinedBlocks` and `MenuScreens.register` in `Blocky13Client`. The GUI/menu code was written against the decompiled 26.2 sources (`extractBackground(GuiGraphicsExtractor)`, `graphics.blit(RenderPipelines.GUI_TEXTURED, …)`, `addStandardInventorySlots`).
- `BlockPlateItem.useOn()` applies a plate to a face: a plain full block becomes a `CombinedBlock` (its block becomes the mining-behaviour "main"), and each clicked face records the plate's block
- `CombinedBlock` (a `BaseEntityBlock`, `RenderShape.MODEL`, no item) delegates hardness to the main block via `getDestroyProgress` and drops the main block + plates in `playerWillDestroy`; `CombinedBlockEntity` stores the main + 6 face blocks and exposes them as Fabric render data
- Rendering uses FRAPI: `CombinedBlockStateModel extends WrapperBlockStateModel`, bound via `ModelLoadingPlugin.modifyBlockModelAfterBake`, emits one quad per face textured with that face block's particle sprite (`getBlockStateModelSet().getParticleMaterial`)
- Functional faces: `CombinedBlock.fallOn`/`stepOn` read the **top** face's block and apply that material's behaviour — slime (bounce + no fall damage), beds (weaker bounce + reduced damage), honey/hay/wool (soft landing), pointed dripstone (2× fall damage), magma (fire damage), cactus (contact damage), slime/honey/soul-sand/mud (sticky walking). Per-face bounce can't use `getBounceRestitution()` (block-level, no position), so bounce is applied manually from `fallDistance` (≈`0.4*√h`). Add more materials by extending the switches in `CombinedBlock`.

**Issue #20 (`CombinedSlabBlock` + client `CombinedSlabStateModel`, reuses #12's FRAPI camo):**
- A full-cube block built from two *different* slabs — one filling the bottom half, another the top. No item; created in-world.
- Trigger is a `UseBlockCallback` registered in `CombinedBlocks.register()` (`combineSlabs`): when a slab item is used on a single slab (`SlabBlock`, `TYPE != DOUBLE`) of a *different* block, and the click hits the empty half (replicating vanilla `SlabBlock#canBeReplaced` geometry), it replaces the target with `COMBINED_SLAB` and stores bottom/top in the BE. Same-material slabs return `PASS` → vanilla makes a normal double slab; world mutation is server-only, returns `SUCCESS` on both sides.
- `CombinedSlabBlock` (`BaseEntityBlock`, `RenderShape.MODEL`, `noOcclusion`) delegates hardness to the **bottom** slab via `getDestroyProgress` and drops both slabs in `playerWillDestroy`; `CombinedSlabBlockEntity` stores the bottom+top `Block`s as Fabric render data.
- `CombinedSlabStateModel extends WrapperBlockStateModel` (bound via `modifyBlockModelAfterBake`) emits a full cube: DOWN cap = bottom sprite, UP cap = top sprite, each side split into two half-height quads (lower = bottom, upper = top). `MutableQuadView.BAKE_LOCK_UV` derives each quad's UVs from world position (side UV-locker uses `1 - y`), so each half samples the matching half of the texture slab-style — no manual UVs needed. Sprites come from each slab's `getParticleMaterial`.

**Floor items (`BookPileBlock`, `PlacedItemBlock` + `mixin/FloorPlaceMixin`, client `PlacedItemRenderer`):**
- `FloorPlaceMixin` injects at HEAD of `Item#useOn`, which runs only after the clicked block's own interaction, so jukeboxes / chiseled bookshelves / enchanting tables are unaffected
- Plain book -> `book_pile` (FACING + BOOKS 1-4, candle-style `canBeReplaced` stacking, block model per count). No item: drops/pick-blocks plain books
- Flat items -> `placed_item` (ITEMS 1-4 for the shape; `PlacedItemBlockEntity` holds the stacks + per-item yaw and drops them in `preRemoveSideEffects`). Placeable = any `JUKEBOX_PLAYABLE` item or the `blocky13:floor_placeable` item tag; written/writable books need sneak (they have their own right-click). Using another one stacks it; empty-hand right-click takes the top item back
- `PlacedItemRenderer` (BER, registered via `BlockEntityRenderers.register`) draws each item lying flat in `ItemDisplayContext.FIXED`, like vanilla's `CampfireRenderer`; the block itself is `RenderShape.INVISIBLE` (its model only provides the particle sprite)
- Assets from `generate_assets.py --floor-items` (`BOOK_PILE_BOOKS`: one 16x16 texture per book color = spine rows 0-1, cover rows 2-13, page edges rows 14-15; `FLOOR_PLACEABLE_ITEMS` = the tag)

## Architecture

**Entry points:**
- `Blocky13.java` — `ModInitializer`; calls `ModBlocks.registerModBlocks()` and `ModItems.register()`
- `Blocky13Client.java` — `ClientModInitializer`; no manual render-layer setup needed (MC 26.1 auto-detects)

**Core registration (`ModBlocks.java`):**
- `BASES` is built by `buildBases()`: 15 single-material bases plus the concrete/terracotta/glass color families, generated programmatically in DyeColor order from MC 26.2's `ColorCollection`s (`Blocks.CONCRETE.pick(c)`, etc.)
- `registerVariants()` loops over them and calls the `register*()` helpers (slab, vertical slab, layer, stairs, fence, fence gate, door, trapdoor, pressure plate, button, chain, bars, wall)
- Powered variants use inner classes (e.g., `PoweredSlab extends SlabBlock`, `PoweredVerticalSlab`, `PoweredMaterialLayer`) that override `isSignalSource()` / `getSignal()`
- `VerticalSlabBlock` (FACING + DOUBLE + WATERLOGGED) and `MaterialLayerBlock` (non-melting `SnowLayerBlock`; `SandLayerBlock` extends it) are the custom block types
- `registerTorchesAndLamps()` registers the 16 colored torches/lamps (issue #9) into the Functional Blocks tab
- Creative tab additions use `CreativeModeTabEvents.modifyOutputEvent()`

**Mixins (`blocky13.mixins.json`, compat level `JAVA_25`):**
- `HoneyClingMixin` (LivingEntity#aiStep TAIL) makes mobs cling to a honey block directly above them (issue #19)

**Dye brush (`DyeBrushItem.java` + `BrushDyeRecipe.java`):**
- `useOn()` maps the target block to its 16-color family and swaps to the appropriate color variant
- Color stored in `DataComponents.DYED_COLOR`; mixing uses leather-armor blending logic

**Asset generation:**
- `generate_assets.py` (Python + Pillow, repo root) generates all JSON model/blockstate/recipe/loot-table files and recolored textures
- Run this script after adding new block variants; do not hand-edit the generated files
- `write_json` writes no trailing newline (match this convention to avoid diff noise)
- Targeted flags re-run one feature without recoloring every material texture:
  `--vertical-slabs`, `--layers`, `--torches-lamps`, `--walls-tags`, `--floor-items`
- Vertical slabs and layers are generated for *every* base discovered from `<base>_slab` blockstates (same pattern as walls), and added to the `minted_variants` mining tags
- Glass (transparent) variants carry `render_type: minecraft:translucent` in their models; preserve it when regenerating

## MC 26.2 / Fabric Conventions

Tooling: Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.152.1+26.2, fabric-loom 1.17-SNAPSHOT, Gradle 9.5.1, Java 25. See memory notes for full details. Key points:
- No `mappings` dependency — Minecraft 26.1+ ships unobfuscated (Mojang names used directly)
- **26.2: per-color vanilla blocks collapsed into `ColorCollection<Block>`** — use `Blocks.WOOL.pick(DyeColor)` / `.white()`…`.black()` instead of `Blocks.WHITE_WOOL`; copper is a `WeatheringCopperCollection` (`Blocks.COPPER_BLOCK.weathering().unaffected()`)
- `ItemGroupEvents` → `CreativeModeTabEvents` (Fabric API rename)
- `BlockRenderLayerMap` removed; render layers are auto-detected (glass models still set `render_type` explicitly)
- Registries: use `Registry.register(BuiltInRegistries.BLOCK, ...)` directly (no Fabric helper wrappers)
- Recipe/loot-table directories are singular: `recipe/`, `loot_table/`
- Item model definitions live under `items/` (not `item/`)
- Mixin `compatibilityLevel` is `JAVA_25`
