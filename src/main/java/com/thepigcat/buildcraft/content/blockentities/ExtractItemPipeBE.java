package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.networking.SyncPipeDirectionPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Optional;

public class ExtractItemPipeBE extends ItemPipeBE {
    /**
     * Pulling every other tick is imperceptible next to the time an item needs to traverse a pipe
     * (1.4-2.5 s), but it halves the number of times we walk the source container's slots - and a
     * drained source means that walk finds nothing, so it always costs the full scan.
     */
    private static final int EXTRACT_INTERVAL = 2;

    private int extractCooldown;

    /** Cached in {@link #cacheTransferSpeed()}: this must not be a per-tick registry lookup. */
    private int maxExtract = 4;

    public ExtractItemPipeBE(BlockPos pos, BlockState blockState) {
        super(BCBlockEntities.EXTRACTING_ITEM_PIPE.get(), pos, blockState);
    }

    @Override
    protected void cacheTransferSpeed() {
        super.cacheTransferSpeed();
        String pipeId = this.getBlockState().getBlock().builtInRegistryHolder().key().location().getPath();
        // 木管道：4 个，绿宝石管道：64 个（1组）
        this.maxExtract = pipeId.contains("emerald") ? 64 : 4;
    }

    /**
     * 获取当前管道的单次最大提取数量。
     */
    protected int getMaxExtract() {
        return maxExtract;
    }

    @Override
    public void tick() {
        if (level.isClientSide()) return;

        if (itemHandler.getStackInSlot(0).isEmpty() && extracting != null) {
            if (--extractCooldown <= 0) {
                extractCooldown = EXTRACT_INTERVAL;
                tryExtract();
            }
        }

        // Regular pipe movement logic
        super.tick();
    }

    private void tryExtract() {
        BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(this.extracting);
        if (cache == null) return;

        IItemHandler extractingHandler = cache.getCapability();
        if (extractingHandler == null) return;

        int amount = getMaxExtract();
        for (int i = 0; i < extractingHandler.getSlots(); i++) {
            ItemStack extracted = extractingHandler.extractItem(i, amount, false);
            if (extracted.isEmpty()) continue;

            // Insert into our pipe
            ItemStack remainder = itemHandler.insertItem(0, extracted, false);
            if (!remainder.isEmpty()) {
                extractingHandler.insertItem(i, remainder, false);
            }

            // Set up direction
            this.setFrom(this.extracting);
            List<Direction> outputs = getValidOutputs();
            Direction bestOutput = chooseBestOutput(outputs, extracted);
            if (bestOutput != null) {
                this.setTo(bestOutput);
            } else if (!outputs.isEmpty()) {
                this.setTo(outputs.getFirst());
            }

            sendToTracking(new SyncPipeDirectionPayload(worldPosition,
                    Optional.ofNullable(from), Optional.ofNullable(to)));
            return;
        }
    }
}
