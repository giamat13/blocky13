package com.blocky13;

import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Issue #20: stores the two slab blocks that make up a {@link CombinedSlabBlock} — the
 * one filling the bottom half and the one filling the top half. The pair is exposed as
 * render data so the client model can texture each half independently (same FRAPI camo
 * approach as {@link CombinedBlockEntity}).
 */
public class CombinedSlabBlockEntity extends BlockEntity implements RenderDataBlockEntity {

    /** Immutable render-data snapshot handed to the client model. */
    public record Data(Block bottom, Block top) {}

    private Block bottom = Blocks.STONE_SLAB;
    private Block top = Blocks.STONE_SLAB;

    public CombinedSlabBlockEntity(BlockPos pos, BlockState state) {
        super(CombinedBlocks.COMBINED_SLAB_ENTITY, pos, state);
    }

    public Block getBottom() {
        return bottom;
    }

    public Block getTop() {
        return top;
    }

    public void setBlocks(Block bottom, Block top) {
        this.bottom = bottom;
        this.top = top;
    }

    /** Persist + push to clients and trigger a re-render of this block. */
    public void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Object getRenderData() {
        return new Data(bottom, top);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("bottom", key(bottom));
        out.putString("top", key(top));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        bottom = block(in.getStringOr("bottom", "minecraft:stone_slab"));
        top = block(in.getStringOr("top", "minecraft:stone_slab"));
        // On the client this is a live sync packet (level is set, unlike the initial chunk
        // load), so re-mesh the section to show the new half textures immediately.
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static String key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
    }
}
