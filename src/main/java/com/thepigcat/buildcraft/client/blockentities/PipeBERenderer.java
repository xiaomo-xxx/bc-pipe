package com.thepigcat.buildcraft.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.content.blockentities.DiamondItemPipeBE;
import com.thepigcat.buildcraft.content.blockentities.ItemPipeBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.function.Function;

public class PipeBERenderer implements BlockEntityRenderer<ItemPipeBE> {
    /**
     * 钻石管道六个方向的面贴图。
     * <p>
     * 这些颜色本来应该由方块模型（pipe_base_colored）提供，但运行时模型会被
     * ModelBakery 里的 mixin 覆盖成中性模型，改资源包也没用（mixin 在所有资源包
     * 加载完之后才注入）。所以这里直接由渲染器把六个面画出来，绕开模型系统。
     */
    private static final ResourceLocation[] DIAMOND_FACE_TEXTURES = new ResourceLocation[6];

    static {
        for (Direction dir : Direction.values()) {
            DIAMOND_FACE_TEXTURES[dir.ordinal()] =
                    ResourceLocation.fromNamespaceAndPath(BuildcraftLegacy.MODID, "block/diamond_pipe_" + dir.getName());
        }
    }

    public PipeBERenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ItemPipeBE pipeBlockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        if (pipeBlockEntity instanceof DiamondItemPipeBE) {
            renderDiamondFaces(poseStack, multiBufferSource, packedLight, packedOverlay);
        }

        ItemStack stack = pipeBlockEntity.getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) return;

        Direction from = pipeBlockEntity.getFrom();
        Direction to = pipeBlockEntity.getTo();

        poseStack.pushPose();
        {
            if (from != null && to != null) {
                // Interpolate movement (may be negative for smooth handoff from previous pipe)
                float progress = Mth.lerp(partialTicks, pipeBlockEntity.lastMovement, pipeBlockEntity.movement);

                Vec3i fromNormal = from.getNormal();
                Vec3i toNormal = to.getNormal();

                // Start: entry face (progress = -0.5)
                // Center: pipe center (progress = 0.0)
                // End: exit face (progress = 1.0)
                // Map: at progress=-0.5 → at "from" face, at progress=0.0 → center, at progress=1.0 → "to" face
                float startX = 0.5f + fromNormal.getX() * 0.5f;
                float startY = 0.5f + fromNormal.getY() * 0.5f;
                float startZ = 0.5f + fromNormal.getZ() * 0.5f;

                float midX = 0.5f;
                float midY = 0.5f;
                float midZ = 0.5f;

                float endX = 0.5f + toNormal.getX() * 0.5f;
                float endY = 0.5f + toNormal.getY() * 0.5f;
                float endZ = 0.5f + toNormal.getZ() * 0.5f;

                float x, y, z;
                // movement 的范围是 -0.5（入口面）→ 1.0（出口面），共 1.5 个单位，对应 1 格距离。
                // 旧代码把两段按不同比例映射（入口 0.5 个单位走半格、出口 1.0 个单位走半格），
                // 物品过了中心速度会突然减半，再叠加 smoothStep 缓动，看起来就是一路顿挫/颤抖。
                // 这里改成统一按 1.5 个单位线性映射，全程匀速。
                float t = Mth.clamp((progress + 0.5f) / 1.5f, 0f, 1f); // 0 = 入口面, 1 = 出口面
                if (t < 0.5f) {
                    // 入口面 → 中心
                    float s = t * 2f;
                    x = Mth.lerp(s, startX, midX);
                    y = Mth.lerp(s, startY, midY);
                    z = Mth.lerp(s, startZ, midZ);
                } else {
                    // 中心 → 出口面
                    float s = (t - 0.5f) * 2f;
                    x = Mth.lerp(s, midX, endX);
                    y = Mth.lerp(s, midY, endY);
                    z = Mth.lerp(s, midZ, endZ);
                }

                poseStack.translate(x, y, z);
            } else {
                // No direction info, float at center
                poseStack.translate(0.5, 0.5, 0.5);
            }

            // Scale: 0.5 for normal items, 0.25 for block items
            float scale = 0.5f;
            if (stack.getItem() instanceof BlockItem) {
                scale = 0.25f;
            }
            poseStack.scale(scale, scale, scale);

            // Rotation: smooth continuous spin like dropped items
            long gameTime = pipeBlockEntity.getLevel() != null ? pipeBlockEntity.getLevel().getGameTime() : 0;
            float tick = gameTime + partialTicks;
            float rotation = tick * 3.0f; // degrees per tick
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

            // Gentle bobbing animation (sinusoidal, like vanilla items)
            float bob = Mth.sin(tick * 0.15f) * 0.03f;
            float bobX = Mth.cos(tick * 0.12f) * 0.02f;
            poseStack.translate(bobX, bob, 0);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.NONE, packedLight, packedOverlay,
                    poseStack, multiBufferSource, pipeBlockEntity.getLevel(), 1
            );
        }
        poseStack.popPose();
    }

    /**
     * 在管体（4..12 那个 8×8×8 立方体）的六个面上各画一张方向色贴图。
     * 画在模型表面外 0.1 像素的位置，避免和模型 z-fighting。
     */
    private static void renderDiamondFaces(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        Function<ResourceLocation, TextureAtlasSprite> atlas =
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

        final float half = 0.25f;       // 8/16 的一半
        final float epsilon = 0.0001f; // 稍微往外挪一点

        for (Direction dir : Direction.values()) {
            TextureAtlasSprite sprite = atlas.apply(DIAMOND_FACE_TEXTURES[dir.ordinal()]);

            poseStack.pushPose();
            // 挪到方块中心，再旋转成朝向 dir，然后沿 +Z 推到该面
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
            poseStack.translate(0f, 0f, half + epsilon);

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();

            consumer.addVertex(matrix, -half, -half, 0f).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0f, 0f, 1f);
            consumer.addVertex(matrix, half, -half, 0f).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0f, 0f, 1f);
            consumer.addVertex(matrix, half, half, 0f).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0f, 0f, 1f);
            consumer.addVertex(matrix, -half, half, 0f).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0f, 0f, 1f);

            poseStack.popPose();
        }
    }
}
