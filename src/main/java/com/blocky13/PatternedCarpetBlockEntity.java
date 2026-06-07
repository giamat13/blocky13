package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PatternedCarpetBlockEntity extends BlockEntity {

    private BannerPatternLayers patterns = BannerPatternLayers.EMPTY;

    public PatternedCarpetBlockEntity(BlockPos pos, BlockState state) {
        super(ModCarpets.CARPET_BLOCK_ENTITY_TYPE, pos, state);
    }

    public BannerPatternLayers getPatterns() {
        return patterns;
    }

    public void setPatterns(BannerPatternLayers p) {
        this.patterns = p;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.patterns.equals(BannerPatternLayers.EMPTY)) {
            output.store("Patterns", BannerPatternLayers.CODEC, this.patterns);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Patterns", BannerPatternLayers.CODEC).ifPresent(p -> this.patterns = p);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
