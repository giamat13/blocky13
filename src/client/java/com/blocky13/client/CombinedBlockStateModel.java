package com.blocky13.client;

import com.blocky13.CombinedBlockEntity;
import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Issue #12: renders a combined block by texturing each of its six faces with the
 * particle sprite of that face's plate block (falling back to the main block). The
 * per-face block data is read from the block entity's render data, so changing a
 * face re-meshes the chunk.
 */
public class CombinedBlockStateModel extends WrapperBlockStateModel {

    public CombinedBlockStateModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    private static CombinedBlockEntity.Data data(BlockAndTintGetter view, BlockPos pos) {
        if (view instanceof FabricBlockGetter fabric
                && fabric.getBlockEntityRenderData(pos) instanceof CombinedBlockEntity.Data d) {
            return d;
        }
        return null;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter view, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        CombinedBlockEntity.Data data = data(view, pos);
        if (data == null) {
            super.emitQuads(emitter, view, pos, state, random, cullTest);
            return;
        }
        BlockStateModelSet models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        for (Direction face : Direction.values()) {
            if (cullTest.test(face)) {
                continue;
            }
            Block plate = data.faces()[face.get3DDataValue()];
            BlockState camo = (plate != null ? plate : data.main()).defaultBlockState();
            Material.Baked sprite = models.getParticleMaterial(camo, view, pos);
            emitter.square(face, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.cullFace(face);
            emitter.uvUnitSquare();
            emitter.materialBake(sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();
        }
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter view, BlockPos pos, BlockState state) {
        CombinedBlockEntity.Data data = data(view, pos);
        if (data != null) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                    .getParticleMaterial(data.main().defaultBlockState(), view, pos);
        }
        return super.particleMaterial(view, pos, state);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter view, BlockPos pos, BlockState state, RandomSource random) {
        CombinedBlockEntity.Data data = data(view, pos);
        if (data == null) {
            return super.createGeometryKey(view, pos, state, random);
        }
        StringBuilder key = new StringBuilder(BuiltInRegistries.BLOCK.getKey(data.main()).toString());
        for (Block face : data.faces()) {
            key.append('|').append(face == null ? "" : BuiltInRegistries.BLOCK.getKey(face).toString());
        }
        return key.toString();
    }
}
