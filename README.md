# Blocky13

A Minecraft Fabric mod that adds decorative and functional block variants for Minecraft 26.2+.

## Features

Blocky13 expands the building possibilities in Minecraft by creating a comprehensive set of variants for base materials. For each base block — 15 metals/minerals (dirt, iron block, coal block, copper block, gold block, redstone block, emerald block, lapis block, diamond block, netherite block, raw iron block, raw copper block, raw gold block, quartz block, amethyst block), the full concrete/terracotta/glass color sets, and 16-color brick blocks — the mod generates:

- **Slabs** — Half-height blocks for detail work
- **Vertical Slabs** — Half-blocks oriented vertically; two matching ones merge into a full block (issue #18)
- **Layers** — Snow-style stackable 1–8 layers of any material (issue #7)
- **Stairs** — Diagonal blocks for elevation changes
- **Fences** — Perimeter blocks with no collision
- **Fence Gates** — Openable fence segments
- **Doors** — Full-size entryways
- **Trapdoors** — Horizontal access blocks
- **Pressure Plates** — Redstone input sensors
- **Buttons** — Redstone input triggers
- **Chains** — Hanging connectors
- **Bars** — Decorative barriers
- **Walls** — Connecting barriers that lower fence gates

Plus **16 colored torches** (standing & wall) and **16 colored lamps** (issue #9), and mobs **cling to honey blocks** from below instead of falling (issue #19).

### Block Crafting & Combined Blocks (issue #12)

Craft a **Block Crafting** station (grass block + crafting table) and right-click it with any block to turn it into 16 thin **block plates**. Right-click the faces of a full block with plates to build a **combined block** — each of its six faces can show a different block's texture, while the block you started from keeps its mining speed. (Face textures are cosmetic for now; functional slime/honey faces are planned.)

## Redstone-Powered Variants

All blocks made from redstone block material have special powered variants that emit a constant redstone signal (strength 15), making them useful for redstone contraptions and automation. These blocks appear in both the Building Blocks and Redstone Blocks creative tabs.

## Requirements

- **Minecraft**: 26.2 or later
- **Fabric Loader**: 0.19.3 or later
- **Java**: 25 or later
- **Fabric API**: 0.152.1+26.2 or later

## Building

Use Gradle to build the mod:

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## Installation

Place the compiled JAR file in your Minecraft mods folder:
- **Windows**: `%APPDATA%/.minecraft/mods/`
- **Linux/Mac**: `~/.minecraft/mods/`

## Development

The mod is structured as a standard Fabric mod project with:

- **Source code**: `src/main/java/com/blocky13/`
- **Client code**: `src/client/java/com/blocky13/client/`
- **Assets**: `src/main/resources/assets/blocky13/`
- **Data**: `src/main/resources/data/blocky13/`

### Key Files

- `ModBlocks.java` — Block registration and factory methods
- `Blocky13.java` — Mod entrypoint
- `blocky13.mixins.json` — Mixin configuration

### Adding New Blocks

To add a new base material variant set:

1. Add the material to `buildBases()` in `ModBlocks.java` with its ID and a vanilla block to copy properties from
2. The registration system automatically generates every variant (slab, vertical slab, layer, stairs, fence, fence gate, door, trapdoor, pressure plate, button, chain, bars, wall)
3. Pass `rs = true` through `registerVariants` for materials whose variants should emit redstone and appear in the Redstone Blocks tab
4. Regenerate the JSON assets, textures and block tags with `python3 generate_assets.py`. Targeted refreshes: `--walls-tags`, `--vertical-slabs`, `--layers`, `--torches-lamps`

## License

This project is licensed under CC0-1.0 — you are free to use, modify, and distribute it.

## Contributing

Contributions are welcome! Feel free to open issues or pull requests for improvements, new block variants, or bug fixes.
