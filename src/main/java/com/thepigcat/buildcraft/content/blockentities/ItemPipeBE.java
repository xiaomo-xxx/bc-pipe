package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.blockentities.PipeBlockEntity;
import com.thepigcat.buildcraft.networking.SyncPipeDirectionPayload;
import com.thepigcat.buildcraft.networking.SyncPipeMovementPayload;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

public class ItemPipeBE extends PipeBlockEntity<IItemHandler> {
    protected final ItemStackHandler itemHandler;

    protected Direction from;
    protected Direction to;
    private Direction prevFrom;

    /**
     * Current progress of the item through this pipe segment.
     * Range: -0.5 (entry face) → 0.0 (center) → 1.0 (exit face)
     * >= 1.0 means the item is ready to transfer out.
     */
    public float movement;
    /** Previous movement value, used for client-side interpolation. */
    public float lastMovement;

    /** How many ticks have passed since last network sync. */
    private int syncTickCounter = 0;
    /** Sync every N ticks (20 ticks = 1 second). Smoother = more bandwidth. */
    private static final int SYNC_INTERVAL = 2;

    // Track how many items each direction has received (for round-robin splitting)
    private final Map<Direction, Integer> itemsSent = new EnumMap<>(Direction.class);

    public ItemPipeBE(BlockPos pos, BlockState blockState) {
        this(BCBlockEntities.ITEM_PIPE.get(), pos, blockState);
    }

    public ItemPipeBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.itemHandler = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    active = !getStackInSlot(0).isEmpty();
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
        for (Direction d : Direction.values()) {
            itemsSent.put(d, 0);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            cacheTransferSpeed();
        }
    }

    /**
     * Cache transfer speed once at load, avoiding per-tick registry lookups.
     */
    protected void cacheTransferSpeed() {
        String pipeId = this.getBlockState().getBlock().builtInRegistryHolder().key().location().getPath();
        double blocksPerSecond;
        if (pipeId.contains("gold")) {
            blocksPerSecond = BCConfig.goldPipeSpeed;
        } else if (pipeId.contains("emerald")) {
            blocksPerSecond = BCConfig.emeraldPipeSpeed;
        } else if (pipeId.contains("void")) {
            blocksPerSecond = BCConfig.voidPipeSpeed;
        } else if (pipeId.contains("diamond")) {
            blocksPerSecond = BCConfig.diamondPipeSpeed;
        } else if (pipeId.contains("wooden")) {
            blocksPerSecond = BCConfig.woodenPipeSpeed;
        } else if (pipeId.contains("iron")) {
            blocksPerSecond = BCConfig.ironPipeSpeed;
        } else {
            blocksPerSecond = BCConfig.basicPipeSpeed;
        }
        // transferSpeed = blocksPerTick (server runs at 20 TPS)
        this.transferSpeed = (float) (blocksPerSecond / 20.0);
    }

    @Override
    protected BlockCapability<IItemHandler, Direction> getCapType() {
        return Capabilities.ItemHandler.BLOCK;
    }

    /**
     * Get all valid output directions (excluding the input direction and directions without handlers)
     */
    protected List<Direction> getValidOutputs() {
        List<Direction> outputs = new ArrayList<>();
        Direction inputDir = this.from;
        ItemStack stack = itemHandler.getStackInSlot(0);

        for (Direction dir : directions) {
            if (dir == inputDir) continue;
            BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(dir);
            if (cache != null && cache.getCapability() != null) {
                // Check if this output can accept our item
                if (canInsertTo(cache.getCapability(), stack)) {
                    outputs.add(dir);
                }
            }
        }
        return outputs;
    }

    /**
     * Check if an item handler can accept a given item
     */
    protected boolean canInsertTo(IItemHandler handler, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack remaining = handler.insertItem(i, stack, true); // simulate
            if (remaining.getCount() < stack.getCount()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Choose the best output direction. Prefer direction that has matching items
     * for compact storage, otherwise round-robin for even distribution.
     */
    protected Direction chooseBestOutput(List<Direction> outputs, ItemStack toSend) {
        if (outputs.isEmpty()) return null;
        if (outputs.size() == 1) return outputs.getFirst();

        // First, try to find a destination that already has matching items
        for (Direction dir : outputs) {
            BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(dir);
            if (cache == null) continue;
            IItemHandler handler = cache.getCapability();
            if (handler == null) continue;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack existing = handler.getStackInSlot(i);
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, toSend)) {
                    return dir;
                }
            }
        }

        // No matching items found, use round-robin (pick least-used direction)
        Direction best = outputs.getFirst();
        int minSent = itemsSent.getOrDefault(best, 0);
        for (Direction dir : outputs) {
            int sent = itemsSent.getOrDefault(dir, 0);
            if (sent < minSent) {
                minSent = sent;
                best = dir;
            }
        }
        return best;
    }

    public void tick() {
        if (level.isClientSide()) return;

        // Fast path: skip all work if pipe has no item and isn't mid-movement
        if (!active && this.movement <= 0f && this.itemHandler.getStackInSlot(0).isEmpty()) return;

        // --- Move item toward the center (entry phase: -0.5 -> 0.0) ---
        if (this.movement < 0f) {
            this.lastMovement = this.movement;
            this.movement += transferSpeed;
            syncTickCounter++;
            if (syncTickCounter >= SYNC_INTERVAL) {
                sendMovementSync();
                syncTickCounter = 0;
            }
            return; // Still approaching center, don't try to output yet
        }

        // Item has reached the end of its movement through this pipe
        if (this.movement >= 1f) {
            ItemStack stack = itemHandler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                List<Direction> outputs = getValidOutputs();
                Direction bestOutput = chooseBestOutput(outputs, stack);

                if (bestOutput != null) {
                    ItemPipeBE nextPipe = BlockUtils.getBE(ItemPipeBE.class, level, worldPosition.relative(bestOutput));

                    if (nextPipe != null && nextPipe.itemHandler.getStackInSlot(0).isEmpty()) {
                        // Transfer item to next pipe for continuous visual flow
                        nextPipe.itemHandler.setStackInSlot(0, stack.copy());
                        itemHandler.setStackInSlot(0, ItemStack.EMPTY);

                        // Set up next pipe direction
                        nextPipe.setFrom(bestOutput.getOpposite());
                        List<Direction> nextOutputs = nextPipe.getValidOutputs();
                        Direction nextTo = nextPipe.chooseBestOutput(nextOutputs, stack);
                        nextPipe.setTo(nextTo != null ? nextTo : nextPipe.from);

                        // Smooth handoff: start next pipe at entry face (progress = -0.5)
                        // so the item appears at the connection point between pipes
                        nextPipe.lastMovement = -0.5f;
                        nextPipe.movement = -0.5f;
                        nextPipe.syncTickCounter = 0;

                        // Deactivate current pipe
                        this.active = false;
                        this.movement = 0f;
                        this.lastMovement = 0f;

                        nextPipe.sendToTracking(new SyncPipeMovementPayload(nextPipe.getBlockPos(), nextPipe.movement, nextPipe.lastMovement));
                        nextPipe.sendToTracking(new SyncPipeDirectionPayload(nextPipe.getBlockPos(),
                                Optional.ofNullable(nextPipe.from), Optional.ofNullable(nextPipe.to)));
                    } else {
                        // Next pipe busy or direct to container — use standard insertion
                        IItemHandler targetHandler = capabilityCaches.get(bestOutput).getCapability();
                        ItemStack remainder = ItemHandlerHelper.insertItem(targetHandler, stack, false);
                        itemsSent.merge(bestOutput, stack.getCount() - remainder.getCount(), Integer::sum);

                        if (remainder.isEmpty()) {
                            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                            this.active = false;
                        } else {
                            itemHandler.setStackInSlot(0, remainder);
                            moveItemBackward();
                        }
                    }
                } else {
                    // No valid outputs, bounce back
                    moveItemBackward();
                }
            }
            this.lastMovement = 0;
            this.movement = 0;
            syncTickCounter = 0;
        } else {
            // --- Advance movement through the pipe (center -> exit: 0.0 -> 1.0) ---
            this.lastMovement = this.movement;
            if (!this.itemHandler.getStackInSlot(0).isEmpty()) {
                this.movement += transferSpeed;
            } else {
                this.movement = 0;
            }
            // Periodic sync for continuous interpolation
            syncTickCounter++;
            if (syncTickCounter >= SYNC_INTERVAL) {
                sendMovementSync();
                syncTickCounter = 0;
            }
        }
    }

    /**
     * Send movement state to all tracking clients for smooth interpolation.
     */
    private void sendMovementSync() {
        sendToTracking(new SyncPipeMovementPayload(getBlockPos(), this.movement, this.lastMovement));
    }

    public void setFrom(Direction from) {
        this.prevFrom = this.from;
        this.from = from;
    }

    public void setTo(Direction to) {
        this.to = to;
    }

    protected void moveItemBackward() {
        Direction oldTo = this.to;
        this.to = from;
        this.from = oldTo;
        this.lastMovement = 0;
        this.movement = 0;

        sendToTracking(new SyncPipeMovementPayload(worldPosition, this.movement, this.lastMovement));
        sendToTracking(new SyncPipeDirectionPayload(worldPosition,
                Optional.ofNullable(from), Optional.ofNullable(this.to)));
    }

    public Direction getFrom() {
        return from;
    }

    public Direction getTo() {
        return to;
    }

    /**
     * Gets the cached transfer speed (blocks per tick).
     */
    protected float getTransferSpeed() {
        return transferSpeed;
    }

    public IItemHandler getItemHandler(Direction direction) {
        return itemHandler;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.itemHandler.deserializeNBT(registries, tag.getCompound("item_handler"));
        int toIndex = tag.getInt("to");
        if (toIndex != -1) {
            this.to = Direction.values()[toIndex];
        }
        int fromIndex = tag.getInt("from");
        if (fromIndex != -1) {
            this.from = Direction.values()[fromIndex];
        }
        this.movement = tag.contains("movement") ? tag.getFloat("movement") : 0f;
        this.lastMovement = tag.contains("lastMovement") ? tag.getFloat("lastMovement") : 0f;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("item_handler", this.itemHandler.serializeNBT(registries));
        tag.putInt("to", to != null ? to.ordinal() : -1);
        tag.putInt("from", from != null ? from.ordinal() : -1);
        tag.putFloat("movement", movement);
        tag.putFloat("lastMovement", lastMovement);
    }
}
