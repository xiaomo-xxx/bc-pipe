package com.thepigcat.buildcraft.client.screens;

import com.thepigcat.buildcraft.content.menus.DiamondPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

/**
 * Diamond pipe filter GUI.
 * <p>
 * 面板不再用贴图，而是直接按 {@code menu.slots} 里的真实坐标画出来 ——
 * 这样槽位外框和物品永远对齐，不会出现贴图版那种错位。
 */
public class DiamondPipeScreen extends AbstractContainerScreen<DiamondPipeMenu> {
    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_DARK = 0xFF373737;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_BG = 0xFF8B8B8B;

    /**
     * 与 diamond_pipe_&lt;face&gt; 贴图的主色一一对应（取自 BCCE 的 diamond_item_&lt;face&gt;）：
     * 下=黑、上=白、北=红、南=蓝、西=绿、东=黄。
     * 顺序必须和 {@code Direction.ordinal()} 一致，因为过滤器槽位就是按这个顺序分组的。
     */
    private static final int[] SIDE_COLORS = {
            0xFF101010,  // DOWN  - 黑
            0xFFF0F0F0,  // UP    - 白
            0xFFE00000,  // NORTH - 红
            0xFF0050E0,  // SOUTH - 蓝
            0xFF20B010,  // WEST  - 绿
            0xFFE0E010,  // EAST  - 黄
    };

    private static final String[] SIDE_LABELS = {"D", "U", "N", "S", "W", "E"};

    /** 浅色块上用深色字，深色块上用白字。 */
    private static final int[] LABEL_COLORS = {
            0xFFFFFFFF, 0xFF303030, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF303030
    };

    private static final int CHIP_X = 4;
    private static final int CHIP_SIZE = 12;

    public DiamondPipeScreen(DiamondPipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 194;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 6;
        this.inventoryLabelX = 26;
        this.inventoryLabelY = 130;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // 面板 + 立体边框
        g.fill(x, y, x + imageWidth, y + imageHeight, PANEL_BG);
        g.fill(x, y, x + imageWidth, y + 1, 0xFF000000);
        g.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF000000);
        g.fill(x, y, x + 1, y + imageHeight, 0xFF000000);
        g.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF000000);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, PANEL_LIGHT);
        g.fill(x + 1, y + 1, x + 2, y + imageHeight - 1, PANEL_LIGHT);
        g.fill(x + 1, y + imageHeight - 2, x + imageWidth - 1, y + imageHeight - 1, PANEL_DARK);
        g.fill(x + imageWidth - 2, y + 1, x + imageWidth - 1, y + imageHeight - 1, PANEL_DARK);

        // 槽位：直接用菜单里的实际坐标，保证和物品严格对齐
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;
            g.fill(sx, sy, sx + 18, sy + 18, SLOT_BG);
            g.fill(sx, sy, sx + 18, sy + 1, PANEL_DARK);
            g.fill(sx, sy, sx + 1, sy + 18, PANEL_DARK);
            g.fill(sx, sy + 17, sx + 18, sy + 18, PANEL_LIGHT);
            g.fill(sx + 17, sy, sx + 18, sy + 18, PANEL_LIGHT);
        }

        // 六个方向的色块，颜色与方块上该面的贴图一致
        for (int side = 0; side < 6; side++) {
            int cx = x + CHIP_X;
            int cy = y + 18 + side * 18 + 2;
            g.fill(cx - 1, cy - 1, cx + CHIP_SIZE + 1, cy + CHIP_SIZE + 1, PANEL_DARK);
            g.fill(cx, cy, cx + CHIP_SIZE, cy + CHIP_SIZE, SIDE_COLORS[side]);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);

        for (int side = 0; side < 6; side++) {
            g.drawString(font, SIDE_LABELS[side],
                    CHIP_X + 3, 22 + side * 18, LABEL_COLORS[side], false);
        }

        g.drawString(font, Component.translatable("container.inventory"),
                inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
