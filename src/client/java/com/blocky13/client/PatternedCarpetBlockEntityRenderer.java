package com.blocky13.client;

import com.blocky13.ModCarpets;
import com.blocky13.PatternedCarpetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.BannerPatternLayers;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;

/**
 * Renders banner patterns flat on the top face of a placed patterned carpet.
 *
 * MC 26.1 uses an extract-then-render model:
 *  - extractRenderState() copies data from the BlockEntity to the render state (game thread)
 *  - render() draws using only the render state (render thread safe)
 *
 * If the BlockEntityRenderer interface has changed to use SubmitNodeCollector instead of
 * MultiBufferSource, replace `MultiBufferSource bufferSource` with
 * `SubmitNodeCollector submitNodeCollector` and adapt the buffer usage accordingly.
 */
public class PatternedCarpetBlockEntityRenderer
        implements BlockEntityRenderer<PatternedCarpetBlockEntity, PatternedCarpetBlockEntityRenderState> {

    private final ModelPart flag;

    public PatternedCarpetBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // In MC 26.1, if Context.bakeLayer() is unavailable use:
        // context.entityModelSet().bakeLayer(ModelLayers.BANNER).getChild("flag")
        ModelPart root = context.bakeLayer(ModelLayers.BANNER);
        this.flag = root.getChild("flag");
    }

    @Override
    public PatternedCarpetBlockEntityRenderState createRenderState() {
        return new PatternedCarpetBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(PatternedCarpetBlockEntity blockEntity,
                                   PatternedCarpetBlockEntityRenderState state,
                                   float partialTick) {
        super.extractRenderState(blockEntity, state, partialTick);
        state.patterns = blockEntity.getPatterns();
        state.color = getColor(blockEntity);
    }

    @Override
    public void render(PatternedCarpetBlockEntityRenderState state,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        List<BannerPatternLayers.Layer> layers = state.patterns.layers();
        if (layers.isEmpty()) return;

        poseStack.pushPose();

        // Center on block, move up to carpet top face (Y=1/16) + tiny anti-z-fight offset
        poseStack.translate(0.5, 1.0 / 16.0 + 0.001, 0.5);

        // Rotate -90° around X so the vertical banner lies flat in the XZ plane
        // After rotation: banner X-axis stays, banner Y-axis maps to +Z
        poseStack.mulPose(Axis.X_POSITIVE.rotationDegrees(-90.0f));

        // Banner flag: 20 wide × 40 tall model units (1 unit = 1/16 block).
        // Scale to cover exactly 1×1 block face:
        //   X: want 1 block = 16 units, flag is 20 units → scale = 16/(20*16) = 0.05
        //   Z: want 1 block = 16 units, flag is 40 units → scale = 16/(40*16) = 0.025
        float sx = 16.0f / (20.0f * 16.0f);  // 0.05
        float sz = 16.0f / (40.0f * 16.0f);  // 0.025
        poseStack.scale(sx, sz, 0.001f);

        // Flag hangs from y=0 downward (y=0 to y=-40 in model space).
        // After rotation y→z, the flag spans z=0 to z=40 from block center.
        // Translate -20 in pre-scale z to center: results in z = -0.5..0.5 in block space.
        poseStack.translate(0.0, 0.0, -20.0);

        // BannerRenderer.renderPatterns renders the base wool color then each pattern layer.
        // Signature (MC 26.1): renderPatterns(PoseStack, MultiBufferSource, int, int, ModelPart, Identifier, DyeColor, List<Layer>, boolean)
        // If the signature differs (e.g. no DyeColor param), replace state.color with nothing
        // and adjust the Identifier to use Sheets.BANNER_SHEET.
        BannerRenderer.renderPatterns(poseStack, bufferSource, packedLight, packedOverlay,
            this.flag, BannerRenderer.BANNER_BASE_TEXTURE, state.color, layers, false);

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
