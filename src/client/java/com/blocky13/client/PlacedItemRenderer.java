package com.blocky13.client;

import com.blocky13.PlacedItemBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the items of a {@code PlacedItemBlock} lying flat on the floor, stacked bottom-up,
 * each turned to the yaw it was placed at (same approach as vanilla's campfire renderer).
 */
public class PlacedItemRenderer implements BlockEntityRenderer<PlacedItemBlockEntity, PlacedItemRenderer.State> {

    /** Items are drawn 10px wide; a flat item model is 1px thick, so each layer is 10/256 of a block. */
    private static final float SCALE = 0.625F;
    private static final float LAYER = SCALE / 16.0F;

    public static class State extends BlockEntityRenderState {
        final List<ItemStackRenderState> items = new ArrayList<>();
        final List<Integer> yaws = new ArrayList<>();
    }

    private final ItemModelResolver itemModelResolver;

    public PlacedItemRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PlacedItemBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.items.clear();
        state.yaws.clear();
        int seed = (int) be.getBlockPos().asLong();
        for (int slot = 0; slot < be.count(); slot++) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            ItemStack stack = be.getItems().get(slot);
            itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, be.getLevel(), null, seed + slot);
            state.items.add(itemState);
            state.yaws.add(be.getYaw(slot));
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int slot = 0; slot < state.items.size(); slot++) {
            ItemStackRenderState itemState = state.items.get(slot);
            if (itemState.isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.002F + LAYER * (slot + 0.5F), 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaws.get(slot)));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(SCALE, SCALE, SCALE);
            itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
