# Blocky13

A Minecraft Fabric mod that adds decorative and functional block variants for Minecraft 26.1+.

## Features

Blocky13 expands the building possibilities in Minecraft by creating a comprehensive set of variants for base materials. For each of 15 different base blocks (dirt, iron block, coal block, copper block, gold block, redstone block, emerald block, lapis block, diamond block, netherite block, raw iron block, raw copper block, raw gold block, quartz block, and amethyst block), the mod generates:

- **Slabs** — Half-height blocks for detail work
- **Stairs** — Diagonal blocks for elevation changes
- **Fences** — Perimeter blocks with no collision
- **Fence Gates** — Openable fence segments
- **Doors** — Full-size entryways
- **Trapdoors** — Horizontal access blocks
- **Pressure Plates** — Redstone input sensors
- **Buttons** — Redstone input triggers
- **Chains** — Hanging connectors
- **Bars** — Decorative barriers

## Redstone-Powered Variants

All blocks made from redstone block material have special powered variants that emit a constant redstone signal (strength 15), making them useful for redstone contraptions and automation. These blocks appear in both the Building Blocks and Redstone Blocks creative tabs.

## Requirements

- **Minecraft**: 26.1 or later
- **Fabric Loader**: 0.19.3 or later
- **Java**: 21 or later
- **Fabric API**: Latest version

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

1. Add the material to the `BASES` array in `ModBlocks.java` with its ID and a vanilla block to copy properties from
2. The registration system automatically generates all 10 variants
3. Specify if the variant should appear in the Redstone Blocks tab via the `registerSlab`/`registerStairs`/etc. calls

## License

This project is licensed under CC0-1.0 — you are free to use, modify, and distribute it.

## Contributing

Contributions are welcome! Feel free to open issues or pull requests for improvements, new block variants, or bug fixes.
