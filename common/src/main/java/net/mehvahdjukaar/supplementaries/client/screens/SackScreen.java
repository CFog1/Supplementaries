package net.mehvahdjukaar.supplementaries.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.inventories.SackContainerMenu;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;


public class SackScreen extends AbstractContainerScreen<SackContainerMenu> {

    public SackScreen(SackContainerMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
    }
//TODO: merge
    private void renderBack(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(ModTextures.SACK_GUI_TEXTURE,x, y, 0, 0, this.imageWidth, this.imageHeight);
    }


    private void renderSlots(GuiGraphics graphics) {
        int k = -1 + (this.width - this.imageWidth) / 2;
        int l = -1 + (this.height - this.imageHeight) / 2;

        int size = CommonConfigs.Functional.SACK_SLOTS.get();


        int[] dims = SackContainerMenu.getRatio(size);
        if (dims[0] > 9) {
            dims[0] = 9;
            dims[1] = (int) Math.ceil(size / 9f);
        }

        int yp = 17 + (18 * 3) / 2 - (9) * dims[1];

        int dimx;
        int xp;
        for (int h = 0; h < dims[1]; ++h) {
            dimx = Math.min(dims[0], size);
            xp = 8 + (18 * 9) / 2 - (dimx * 18) / 2;
            for (int j = 0; j < dimx; ++j) {
                graphics. blit(ModTextures.SLOT_TEXTURE, k + xp + j * 18, l + yp + 18 * h, 0, 0, 18, 18, 18, 18);
            }
            size -= dims[0];
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.renderBack(graphics, partialTicks, mouseX, mouseY);
        this.renderSlots(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }
}

