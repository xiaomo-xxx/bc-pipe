package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.api.blockentities.AbstractTransportPipeBE;
import com.thepigcat.buildcraft.networking.SyncPipeDirectionPayload;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 物品管道。运输进度的公共部分见 {@link AbstractTransportPipeBE}，
 * 这里只处理「物品」相关的部分：槽位、能否接收、输出选择。
 */
public class ItemPipeBE extends AbstractTransportPipeBE<IItemHandler> {
    protected final ItemStackHandler itemHandler;

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
        if (outputs.size() == 1) {
            preferredOutput = outputs.getFirst();
            return preferredOutput;
        }

        // Fast path: item flow is stable in practice, so the side we used last tick is almost
        // always the right one again. Verifying a single handler is far cheaper than walking
        // every slot of every output.
        if (preferredOutput != null && outputs.contains(preferredOutput)
                && holdsMatchingItem(preferredOutput, toSend)) {
            return preferredOutput;
        }

        // First, try to find a destination that already has matching items
        for (Direction dir : outputs) {
            if (holdsMatchingItem(dir, toSend)) {
                preferredOutput = dir;
                return dir;
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
        preferredOutput = best;
        return best;
    }

    /**
     * Whether the container behind {@code dir} already holds the given item, i.e. sending it there
     * keeps the destination compact instead of scattering one item per slot.
     */
    private boolean holdsMatchingItem(Direction dir, ItemStack toSend) {
        BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(dir);
        if (cache == null) return false;
        IItemHandler handler = cache.getCapability();
        if (handler == null) return false;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack existing = handler.getStackInSlot(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, toSend)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (level.isClientSide()) {
            tickClient();
            return;
        }

        // Fast path: skip all work if pipe has no item and isn't mid-movement
        if (!active && this.movement <= 0f && this.itemHandler.getStackInSlot(0).isEmpty()) return;

        // --- Move item toward the center (entry phase: -0.5 -> 0.0) ---
        if (this.movement < 0f) {
            this.lastMovement = this.movement;
            this.movement += transferSpeed;
            queueMovementSync();
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

                        // Deactivate current pipe
                        this.active = false;
                        this.movement = 0f;
                        this.lastMovement = 0f;

                        nextPipe.queueMovementSync();
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
        } else if (this.movement >= 0f) {
            // --- Advance movement through the pipe (center -> exit: 0.0 -> 1.0) ---
            this.lastMovement = this.movement;
            if (!this.itemHandler.getStackInSlot(0).isEmpty()) {
                this.movement += transferSpeed;
            } else {
                this.movement = 0;
            }
            queueMovementSync();
        }
    }

    /**
     * 回弹时额外把 from/to 同步给客户端 —— 在途物品的渲染方向依赖这两个值。
     */
    protected void moveItemBackward() {
        super.moveBackward();
        sendToTracking(new SyncPipeDirectionPayload(worldPosition,
                Optional.ofNullable(from), Optional.ofNullable(this.to)));
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
        loadTransport(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("item_handler", this.itemHandler.serializeNBT(registries));
        saveTransport(tag);
    }
}
