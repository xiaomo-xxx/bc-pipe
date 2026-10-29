package com.thepigcat.buildcraft.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.content.blockentities.ItemPipeBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Function;

/**
 * 钻石管道：在「渲染管内物品」的基础上，把管体六个面画成带方向色的贴图。
 * <p>
 * 之前想通过 block model 的 per-face texture 实现，但方块渲染用的模型是运行时由
 * mixin 动态生成的，资源文件和资源包都覆盖不了。这里改用渲染器，完全绕开模型系统。
 * <p>
 * 用的是 {@code diamond_pipe_<face>.png} 本身（彩色方块 + 青色边框），
 * 而不是纯色薄片 —— 纯色看着像贴了六张彩纸，没有管道质感。
 */
public class DiamondPipeBERenderer extends PipeBERenderer {
    /**
     * 管体是模型坐标 4..12 的立方体，换算成方块坐标是 0.25..0.75。
     * <p>
     * 注意：{@code addVertex} 用的是方块坐标（0~1），不是模型坐标（0~16）。
     */
    private static final float HALF = 8f / 16f / 2f;      // 0.25
    private static final float EPSILON = 0.0005f;

    private static final ResourceLocation[] FACE_TEXTURES = new ResourceLocation[6];

    static {
        for (Direction dir : Direction.values()) {
            FACE_TEXTURES[dir.ordinal()] = ResourceLocation.fromNamespaceAndPath(
                    BuildcraftLegacy.MODID, "block/diamond_pipe_" + dir.getName());
        }
    }

    public DiamondPipeBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull ItemPipeBE pipeBlockEntity, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        // 先让父类画管内的物品
        super.render(pipeBlockEntity, partialTicks, poseStack, multiBufferSource, packedLight, packedOverlay);

        VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        Function<ResourceLocation, TextureAtlasSprite> atlas =
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

        for (Direction dir : Direction.values()) {
            TextureAtlasSprite sprite = atlas.apply(FACE_TEXTURES[dir.ordinal()]);

            poseStack.pushPose();
            // 移到方块中心，旋转成朝向 dir，再沿 +Z 推到该面外侧
            poseStack.translate(0.5f, 0.5f, 0.5f);
            switch (dir) {
                case SOUTH -> {
                }
                case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
                case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            }
            poseStack.translate(0f, 0f, HALF + EPSILON);

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();

            consumer.addVertex(matrix, -HALF, -HALF, 0f).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0f, 0f, 1f);
            consumer.addVertex(matrix, HALF, -HALF, 0f).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0f, 0f, 1f);
            consumer.addVertex(matrix, HALF, HALF, 0f).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0f, 0f, 1f);
            consumer.addVertex(matrix, -HALF, HALF, 0f).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0f, 0f, 1f);

            poseStack.popPose();
        }
    }
}
