package com.blocky13;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.patterns.equals(BannerPatternLayers.EMPTY)) {
            BannerPatternLayers.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.patterns)
                .resultOrPartial()
                .ifPresent(nbt -> tag.put("Patterns", nbt));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Patterns")) {
            BannerPatternLayers.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("Patterns"))
                .resultOrPartial()
                .ifPresent(p -> this.patterns = p);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}
