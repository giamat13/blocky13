package com.blocky13.client;

import com.blocky13.ModCarpets;
import com.blocky13.PatternedCarpetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class PatternedCarpetBlockEntityRenderer
        implements BlockEntityRenderer<PatternedCarpetBlockEntity, PatternedCarpetBlockEntityRenderState> {

    private final BannerFlagModel flagModel;
    private final SpriteGetter sprites;

    public PatternedCarpetBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.flagModel = new BannerFlagModel(context.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.sprites = context.sprites();
    }

    @Override
    public PatternedCarpetBlockEntityRenderState createRenderState() {
        return new PatternedCarpetBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(PatternedCarpetBlockEntity blockEntity,
                                   PatternedCarpetBlockEntityRenderState state,
                                   float partialTick,
                                   Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.patterns = blockEntity.getPatterns();
        state.color = getColor(blockEntity);
    }

    @Override
    public void submit(PatternedCarpetBlockEntityRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        if (state.patterns.layers().isEmpty()) return;

        poseStack.pushPose();

        // Center on block, raise to carpet top face + tiny anti-z-fight offset
        poseStack.translate(0.5, 1.0 / 16.0 + 0.001, 0.5);

        // Rotate -90° around X so the vertical banner flag lies flat on the XZ plane
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));

        // Scale flag (20×40 model units) to fit a 1×1 block face
        float sx = 16.0f / (20.0f * 16.0f);
        float sz = 16.0f / (40.0f * 16.0f);
        poseStack.scale(sx, sz, 0.001f);

        // Center the flag horizontally (flag hangs from y=0 downward in model space)
        poseStack.translate(0.0, 0.0, -20.0);

        BannerRenderer.submitPatterns(this.sprites, poseStack, collector,
                state.lightCoords, OverlayTexture.NO_OVERLAY,
                this.flagModel, 0.0f, false,
                state.color, state.patterns, state.breakProgress);

        poseStack.popPose();
    }

    private static DyeColor getColor(PatternedCarpetBlockEntity be) {
        Block block = be.getBlockState().getBlock();
        for (Map.Entry<DyeColor, ?> entry : ModCarpets.CARPETS.entrySet()) {
            if (entry.getValue() == block) return entry.getKey();
        }
        return DyeColor.WHITE;
    }
}
