package com.blocky13.client;

import com.blocky13.BlockCraftingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Issue #12 GUI screen. Reuses the vanilla stonecutter background (its input/result
 * slot positions match {@link BlockCraftingMenu}), so only the background blit is
 * custom — slot, item and label rendering come from {@link AbstractContainerScreen}.
 */
public class BlockCraftingScreen extends AbstractContainerScreen<BlockCraftingMenu> {

    private static final Identifier BG_LOCATION =
            Identifier.withDefaultNamespace("textures/gui/container/stonecutter.png");

    public BlockCraftingScreen(BlockCraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelY -= 1;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, this.leftPos, this.topPos,
                0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }
}
