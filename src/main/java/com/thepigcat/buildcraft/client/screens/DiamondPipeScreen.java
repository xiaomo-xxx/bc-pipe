package com.thepigcat.buildcraft.client.screens;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.content.menus.DiamondPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Diamond pipe filter GUI.
 * <p>
 * 6 rows, one per direction, 9 filter slots each. The colour chip on the left of every row
 * matches the actual face texture of that direction (see the {@code diamond_pipe_<face>} textures),
 * so what you see in the GUI is what you see on the block.
 */
public class DiamondPipeScreen extends AbstractContainerScreen<DiamondPipeMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BuildcraftLegacy.MODID, "textures/gui/diamond_pipe.png");

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
        this.inventoryLabelY = 130;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 每个方向一个色块，颜色与方块上该面的贴图一致
        for (int side = 0; side < 6; side++) {
            int chipY = y + 18 + side * 18 + 3;
            // 1px 深色描边
            guiGraphics.fill(x + CHIP_X - 1, chipY - 1,
                    x + CHIP_X + CHIP_SIZE + 1, chipY + CHIP_SIZE + 1, 0xFF373737);
            guiGraphics.fill(x + CHIP_X, chipY,
                    x + CHIP_X + CHIP_SIZE, chipY + CHIP_SIZE, SIDE_COLORS[side]);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);

        for (int side = 0; side < 6; side++) {
            int labelY = 22 + side * 18;
            guiGraphics.drawString(font, SIDE_LABELS[side],
                    CHIP_X + 3, labelY, LABEL_COLORS[side], false);
        }

        guiGraphics.drawString(font, Component.translatable("container.inventory"),
                inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
