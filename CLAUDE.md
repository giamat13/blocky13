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

Recently implemented issues: #7 (snow-style layer blocks for every base), #9 (colored torches & lamps), #12 (Block Crafting station + per-face "combined" blocks), #18 (vertical slabs), #19 (mobs cling to a honey block from below). Issue #20 (a double slab of two *different* slabs) is still deferred.

**Issue #12 (`CombinedBlocks.java` + client `CombinedBlockStateModel.java`):**
- `BlockCraftingBlock` (the "Block Crafting" station, crafted from grass block + crafting table; distinct grass-topped-workbench texture from `create_block_crafting_textures`): any non-sneak right-click opens a real GUI — `BlockCraftingMenu` (server, Stonecutter-style: input slot 0 → result slot 1 with 16 `BlockPlateItem` plates of the input block, stored in the `PLATE_BLOCK` data component) + client `BlockCraftingScreen` (reuses the vanilla `stonecutter.png` background; only `extractBackground` is custom). Registered via `MenuType` in `CombinedBlocks` and `MenuScreens.register` in `Blocky13Client`. The GUI/menu code was written against the decompiled 26.2 sources (`extractBackground(GuiGraphicsExtractor)`, `graphics.blit(RenderPipelines.GUI_TEXTURED, …)`, `addStandardInventorySlots`).
- `BlockPlateItem.useOn()` applies a plate to a face: a plain full block becomes a `CombinedBlock` (its block becomes the mining-behaviour "main"), and each clicked face records the plate's block
- `CombinedBlock` (a `BaseEntityBlock`, `RenderShape.MODEL`, no item) delegates hardness to the main block via `getDestroyProgress` and drops the main block + plates in `playerWillDestroy`; `CombinedBlockEntity` stores the main + 6 face blocks and exposes them as Fabric render data
- Rendering uses FRAPI: `CombinedBlockStateModel extends WrapperBlockStateModel`, bound via `ModelLoadingPlugin.modifyBlockModelAfterBake`, emits one quad per face textured with that face block's particle sprite (`getBlockStateModelSet().getParticleMaterial`)
- Functional faces: `CombinedBlock.fallOn`/`stepOn` read the **top** face's block and apply that material's behaviour — slime (bounce + no fall damage), beds (weaker bounce + reduced damage), honey/hay/wool (soft landing), pointed dripstone (2× fall damage), magma (fire damage), cactus (contact damage), slime/honey/soul-sand/mud (sticky walking). Per-face bounce can't use `getBounceRestitution()` (block-level, no position), so bounce is applied manually from `fallDistance` (≈`0.4*√h`). Add more materials by extending the switches in `CombinedBlock`.

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
  `--vertical-slabs`, `--layers`, `--torches-lamps`, `--walls-tags`
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
