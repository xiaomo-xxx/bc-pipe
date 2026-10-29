package com.thepigcat.buildcraft.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thepigcat.buildcraft.content.blockentities.ItemPipeBE;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 钻石管道：在原有「渲染管内物品」的基础上，额外画出六个方向色。
 * <p>
 * 之前一直想通过 block model 的 per-face texture 实现，但无论改 datagen 产物还是加资源包，
 * 方块上始终只有中性青色 —— 说明渲染用的模型是运行时动态生成的，资源文件覆盖不了。
 * 这里直接用渲染器在管体六个面外侧画一层纯色薄片，完全绕开模型系统。
 * <p>
 * 颜色顺序与 {@code DiamondPipeScreen.SIDE_COLORS} 一致，按 {@code Direction.get3DDataValue()} 排列：
 * 0=下 1=上 2=北 3=南 4=西 5=东。
 */
public class DiamondPipeBERenderer extends PipeBERenderer {
    /** 纯白贴图，配合顶点色染色。 */
    private static final ResourceLocation WHITE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static final int[] SIDE_COLORS = {
            0x101010,  // DOWN  - 黑
            0xF0F0F0,  // UP    - 白
            0xE00000,  // NORTH - 红
            0x0050E0,  // SOUTH - 蓝
            0x20B010,  // WEST  - 绿
            0xE0E010,  // EAST  - 黄
    };

    /**
     * 管体是模型坐标 4..12 的立方体，换算成方块坐标就是 0.25..0.75。
     * <p>
     * 注意：{@code addVertex} 用的是方块坐标（0~1），不是模型坐标（0~16）。
     * 之前这里直接写了 3.995 / 12.005，等于把色片画到了离管道 4~12 格远的地方，
     * 所以方块上一直看不到颜色。色片稍微外扩一点盖住模型面，避免 z-fighting。
     */
    private static final float LO = 4f / 16f - 0.0005f;
    private static final float HI = 12f / 16f + 0.0005f;

    public DiamondPipeBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull ItemPipeBE pipeBlockEntity, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        // 先让父类画管内的物品
        super.render(pipeBlockEntity, partialTicks, poseStack, multiBufferSource, packedLight, packedOverlay);

        VertexConsumer vc = multiBufferSource.getBuffer(RenderType.entitySolid(WHITE));
        PoseStack.Pose pose = poseStack.last();

        // 下 (y = LO)
        quad(vc, pose, packedLight, packedOverlay, SIDE_COLORS[0],
                LO, LO, LO, HI, LO, LO, HI, LO, HI, LO, LO, HI, 0, -1, 0);
        // 上 (y = HI)
        quad(vc, pose, packedLight, packedOverlay, SIDE_COLORS[1],
                LO, HI, LO, LO, HI, HI, HI, HI, HI, HI, HI, LO, 0, 1, 0);
        // 北 (z = LO)
        quad(vc, pose, packedLight, packedOverlay, SIDE_COLORS[2],
                HI, LO, LO, LO, LO, LO, LO, HI, LO, HI, HI, LO, 0, 0, -1);
        // 南 (z = HI)
        quad(vc, pose, packedLight, packedOverlay, SIDE_COLORS[3],
                LO, LO, HI, HI, LO, HI, HI, HI, HI, LO, HI, HI, 0, 0, 1);
        // 西 (x = LO)
        quad(vc, pose, packedLight, packedOverlay, SIDE_COLORS[4],
                LO, LO, LO, LO, LO, HI, LO, HI, HI, LO, HI, LO, -1, 0, 0);
        // 东 (x = HI)
        quad(vc, pose, packedLight, packedOverlay, SIDE_COLORS[5],
                HI, LO, HI, HI, LO, LO, HI, HI, LO, HI, HI, HI, 1, 0, 0);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, int light, int overlay, int rgb,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float nx, float ny, float nz) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, 255).setUv(0.0f, 0.0f)
                .setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, 255).setUv(1.0f, 0.0f)
                .setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, 255).setUv(1.0f, 1.0f)
                .setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x4, y4, z4).setColor(r, g, b, 255).setUv(0.0f, 1.0f)
                .setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
