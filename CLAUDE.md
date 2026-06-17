# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew build       # builds build/libs/blocky13-1.0.0.jar
./gradlew runClient   # launches a dev Minecraft client
```

No test suite exists. Verification is done by running the client.

## Project Overview

Blocky13 is a Fabric mod for Minecraft 26.1.2 that adds decorative/functional block variants (slabs, stairs, fences, fence gates, doors, trapdoors, pressure plates, buttons, chains, bars, walls) for 15 base materials, plus 16-color brick blocks and a sand layer block. Redstone-material variants emit a constant redstone signal of 15.

A `DyeBrushItem` lets players recolor wool, terracotta, glass, and concrete blocks in-world using dyes.

## Architecture

**Entry points:**
- `Blocky13.java` — `ModInitializer`; calls `ModBlocks.registerModBlocks()` and `ModItems.register()`
- `Blocky13Client.java` — `ClientModInitializer`; no manual render-layer setup needed (MC 26.1 auto-detects)

**Core registration (`ModBlocks.java`):**
- `BASES` array lists the 15 source materials
- `registerVariants()` loops over them and calls 11 `register*()` helpers
- Powered variants use inner classes (e.g., `PoweredSlab extends SlabBlock`) that override `isSignalSource()` / `getSignal()`
- Creative tab additions use `CreativeModeTabEvents.modifyOutputEvent()`

**Dye brush (`DyeBrushItem.java` + `BrushDyeRecipe.java`):**
- `useOn()` maps the target block to its 16-color family and swaps to the appropriate color variant
- Color stored in `DataComponents.DYED_COLOR`; mixing uses leather-armor blending logic

**Asset generation:**
- `generate_assets.py` (Python, repo root) generates all JSON model/blockstate/recipe/loot-table files
- Run this script after adding new block variants; do not hand-edit the generated files

## MC 26.1 / Fabric Conventions

See memory notes for full details. Key points:
- No `mappings` dependency — Minecraft 26.1+ ships unobfuscated (Mojang names used directly)
- Use `fabric-loom` plugin (v1.16-SNAPSHOT); drop the `mappings` line
- `ItemGroupEvents` → `CreativeModeTabEvents` (Fabric API rename)
- `BlockRenderLayerMap` removed; render layers are auto-detected
- Registries: use `Registry.register(BuiltInRegistries.BLOCK, ...)` directly (no Fabric helper wrappers)
- Recipe/loot-table directories are singular: `recipe/`, `loot_table/`
- Item model definitions live under `items/` (not `item/`)
