package com.blocky13;

/**
 * The original standalone sand layer block. Kept as a named subclass for clarity;
 * the non-melting layer behaviour now lives in {@link MaterialLayerBlock}.
 */
public class SandLayerBlock extends MaterialLayerBlock {

    public SandLayerBlock(Properties properties) {
        super(properties);
    }
}
