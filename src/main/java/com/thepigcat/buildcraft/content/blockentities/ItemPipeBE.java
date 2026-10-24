package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.blockentities.PipeBlockEntity;
import com.thepigcat.buildcraft.networking.PipeSyncManager;
import com.thepigcat.buildcraft.networking.SyncPipeDirectionPayload;
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

    /**
     * 客户端专用：服务端最近一次同步过来的目标进度。
     * <p>
     * 网络包到达的时刻并不均匀（服务端 tick 抖动 + 网络抖动），如果直接拿包里的值渲染，
     * 物品位置会每帧跳一下，看起来就是「抽搐」。客户端每 tick 只朝这个目标逼近一部分，
     * 运动就始终连续。
     */
    public float targetMovement;

    /**
     * Last side an item was successfully routed to. Kept so the hot path can skip the
     * "scan every output for a matching stack" loop that {@link #chooseBestOutput} otherwise runs
     * on every single transfer.
     */
    protected Direction preferredOutput;

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
     * 客户端每 tick 把 movement 朝服务端目标值逼近一半。
     * 这样无论数据包什么时候到达，渲染用的位置都是连续变化的。
     */
    protected void tickClient() {
        this.lastMovement = this.movement;

        float delta = this.targetMovement - this.movement;
        if (Math.abs(delta) < 0.002f || Math.abs(delta) > 0.6f) {
            // 已经对齐；或者差值过大（换物品、反向回弹），本来就该瞬间变化，直接对齐
            this.movement = this.targetMovement;
        } else {
            this.movement += delta * 0.5f;
        }
    }

    /**
     * Hands this pipe's current movement state to {@link PipeSyncManager}, which flushes one
     * packet per chunk at the end of the server tick instead of one packet per pipe.
     */
    protected void queueMovementSync() {
        if (level instanceof ServerLevel serverLevel) {
            PipeSyncManager.queueMovement(serverLevel, worldPosition, this.movement);
        }
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

        queueMovementSync();
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
        // 方块实体数据同步过来的值作为初始目标，之后由网络包持续更新
        this.targetMovement = this.movement;
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
