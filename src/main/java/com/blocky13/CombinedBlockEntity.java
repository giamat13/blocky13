package com.blocky13;

import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Stores the main block (mining behaviour) and the per-face plate blocks of a
 * {@link CombinedBlock}. The face data is exposed as render data so the client
 * model can texture each face independently.
 */
public class CombinedBlockEntity extends BlockEntity implements RenderDataBlockEntity {

    /** Immutable render-data snapshot handed to the client model. */
    public record Data(Block main, Block[] faces) {}

    private Block mainBlock = Blocks.STONE;
    private final Block[] faces = new Block[6];

    public CombinedBlockEntity(BlockPos pos, BlockState state) {
        super(CombinedBlocks.COMBINED_BLOCK_ENTITY, pos, state);
    }

    public Block getMainBlock() {
        return mainBlock;
    }

    public void setMainBlock(Block block) {
        this.mainBlock = block;
    }

    /** The plate on a face, or {@code null} if that face shows the main block. */
    public Block getRawFace(Direction face) {
        return faces[face.get3DDataValue()];
    }

    /** The block whose texture a face shows (the plate, or the main block as a fallback). */
    public Block getFace(Direction face) {
        Block plate = faces[face.get3DDataValue()];
        return plate != null ? plate : mainBlock;
    }

    public void setFace(Direction face, Block block) {
        faces[face.get3DDataValue()] = block;
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
        return new Data(mainBlock, faces.clone());
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("main", key(mainBlock));
        for (int i = 0; i < faces.length; i++) {
            if (faces[i] != null) {
                out.putString("face" + i, key(faces[i]));
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        mainBlock = block(in.getStringOr("main", "minecraft:stone"));
        for (int i = 0; i < faces.length; i++) {
            String id = in.getStringOr("face" + i, "");
            faces[i] = id.isEmpty() ? null : block(id);
        }
        // When this runs on the client it is a live sync packet (level is set, unlike the
        // initial chunk load where level is still null), so re-mesh the section to show the
        // new face textures immediately instead of only after a reload.
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
