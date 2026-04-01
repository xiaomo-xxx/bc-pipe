package com.thepigcat.buildcraft.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thepigcat.buildcraft.content.blockentities.ItemPipeBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PipeBERenderer implements BlockEntityRenderer<ItemPipeBE> {
    public PipeBERenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ItemPipeBE pipeBlockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = pipeBlockEntity.getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) return;

        Direction from = pipeBlockEntity.getFrom();
        Direction to = pipeBlockEntity.getTo();

        // Smooth interpolation with eased progress
        float rawProgress = Mth.lerp(partialTicks, pipeBlockEntity.lastMovement, pipeBlockEntity.movement);
        // Clamp to [0, 1]
        rawProgress = Mth.clamp(rawProgress, 0f, 1f);

        poseStack.pushPose();
        {
            if (from != null && to != null) {
                // Item moves from "from" side center toward "to" side center
                // At progress=0: item is at from side
                // At progress=1: item is at to side
                Vec3i fromNormal = from.getNormal();
                Vec3i toNormal = to.getNormal();

                // Start position: center of the "from" connection face
                float startX = 0.5f + fromNormal.getX() * 0.5f;
                float startY = 0.5f + fromNormal.getY() * 0.5f;
                float startZ = 0.5f + fromNormal.getZ() * 0.5f;

                // End position: center of the "to" connection face
                float endX = 0.5f + toNormal.getX() * 0.5f;
                float endY = 0.5f + toNormal.getY() * 0.5f;
                float endZ = 0.5f + toNormal.getZ() * 0.5f;

                // Smooth ease-in-out interpolation
                float t = smoothStep(rawProgress);

                float x = Mth.lerp(t, startX, endX);
                float y = Mth.lerp(t, startY, endY);
                float z = Mth.lerp(t, startZ, endZ);

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

            // Gentle bobbing animation while in pipe
            float bob = Mth.sin((pipeBlockEntity.getLevel() != null ? pipeBlockEntity.getLevel().getGameTime() : 0) + partialTicks) * 0.02f;
            poseStack.translate(0, bob, 0);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.NONE, packedLight, packedOverlay,
                    poseStack, multiBufferSource, pipeBlockEntity.getLevel(), 1
            );
        }
        poseStack.popPose();
    }

    /**
     * Smooth-step easing for natural acceleration/deceleration.
     */
    private static float smoothStep(float t) {
        return t * t * (3f - 2f * t);
    }
}
