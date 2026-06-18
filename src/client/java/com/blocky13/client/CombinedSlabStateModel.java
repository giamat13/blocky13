package com.blocky13.client;

import com.blocky13.CombinedSlabBlockEntity;
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
 * Issue #20: renders a combined slab as a full cube whose bottom half is textured with the
 * bottom slab's sprite and top half with the top slab's. The side faces are split into two
 * half-height quads; {@link MutableQuadView#BAKE_LOCK_UV} derives each quad's UVs from its
 * world position, so each half samples the matching half of the texture (slab-style) for
 * free. The slab pair is read from the block entity's render data, so changing it re-meshes
 * the chunk (mirrors {@link CombinedBlockStateModel}).
 */
public class CombinedSlabStateModel extends WrapperBlockStateModel {

    private static final Direction[] HORIZONTALS =
            {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public CombinedSlabStateModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    private static CombinedSlabBlockEntity.Data data(BlockAndTintGetter view, BlockPos pos) {
        if (view instanceof FabricBlockGetter fabric
                && fabric.getBlockEntityRenderData(pos) instanceof CombinedSlabBlockEntity.Data d) {
            return d;
        }
        return null;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter view, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        CombinedSlabBlockEntity.Data data = data(view, pos);
        if (data == null) {
            super.emitQuads(emitter, view, pos, state, random, cullTest);
            return;
        }
        BlockStateModelSet models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        Material.Baked bottom = models.getParticleMaterial(data.bottom().defaultBlockState(), view, pos);
        Material.Baked top = models.getParticleMaterial(data.top().defaultBlockState(), view, pos);

        // Outer caps: the bottom slab's sprite on DOWN, the top slab's on UP.
        if (!cullTest.test(Direction.DOWN)) {
            emitFace(emitter, Direction.DOWN, 0.0f, 1.0f, bottom);
        }
        if (!cullTest.test(Direction.UP)) {
            emitFace(emitter, Direction.UP, 0.0f, 1.0f, top);
        }
        // Sides: lower half bottom sprite, upper half top sprite.
        for (Direction face : HORIZONTALS) {
            if (cullTest.test(face)) {
                continue;
            }
            emitFace(emitter, face, 0.0f, 0.5f, bottom);
            emitFace(emitter, face, 0.5f, 1.0f, top);
        }
    }

    /** Emit one quad on {@code face} spanning vertical extent {@code [bottom, top]}, baked from {@code sprite}. */
    private static void emitFace(QuadEmitter emitter, Direction face, float bottom, float top, Material.Baked sprite) {
        emitter.square(face, 0.0f, bottom, 1.0f, top, 0.0f);
        emitter.materialBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.color(-1, -1, -1, -1);
        emitter.emit();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter view, BlockPos pos, BlockState state) {
        CombinedSlabBlockEntity.Data data = data(view, pos);
        if (data != null) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                    .getParticleMaterial(data.bottom().defaultBlockState(), view, pos);
        }
        return super.particleMaterial(view, pos, state);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter view, BlockPos pos, BlockState state, RandomSource random) {
        CombinedSlabBlockEntity.Data data = data(view, pos);
        if (data == null) {
            return super.createGeometryKey(view, pos, state, random);
        }
        return BuiltInRegistries.BLOCK.getKey(data.bottom()) + "|" + BuiltInRegistries.BLOCK.getKey(data.top());
    }
}
