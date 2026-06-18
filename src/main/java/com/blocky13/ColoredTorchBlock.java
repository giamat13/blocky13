package com.blocky13;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.TorchBlock;

/**
 * Issue #9: a standing colored torch. Thin subclass only exists because
 * {@link TorchBlock}'s constructor is protected.
 */
public class ColoredTorchBlock extends TorchBlock {

    public ColoredTorchBlock(SimpleParticleType flameParticle, Properties properties) {
        super(flameParticle, properties);
    }
}
