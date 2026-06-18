package com.blocky13;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.WallTorchBlock;

/**
 * Issue #9: the wall-mounted form of a colored torch. Thin subclass only exists
 * because {@link WallTorchBlock}'s constructor is protected.
 */
public class ColoredWallTorchBlock extends WallTorchBlock {

    public ColoredWallTorchBlock(SimpleParticleType flameParticle, Properties properties) {
        super(flameParticle, properties);
    }
}
