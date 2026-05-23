package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.content.menus.DiamondPipeMenu;
import com.thepigcat.buildcraft.networking.SyncPipeDirectionPayload;
import com.thepigcat.buildcraft.networking.SyncPipeMovementPayload;
import com.thepigcat.buildcraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Diamond pipe with 6 filter sides (9 slots each = 54 total).
 * Items can only go to sides where the filter matches (or filter is empty).
 */
public class DiamondItemPipeBE extends ItemPipeBE implements MenuProvider {
    public static final int SLOTS_PER_SIDE = 9;
    public static final int TOTAL_FILTER_SLOTS = SLOTS_PER_SIDE * 6; // 54

    private final ItemStackHandler filterHandler = new ItemStackHandler(TOTAL_FILTER_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public DiamondItemPipeBE(BlockPos pos, BlockState blockState) {
        super(BCBlockEntities.DIAMOND_ITEM_PIPE.get(), pos, blockState);
    }

    @Override
    public void tick() {
        if (level.isClientSide()) return;

        // Extraction logic for diamond pipe (every tick for smooth flow)
        if (itemHandler.getStackInSlot(0).isEmpty() && extracting != null) {
            BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(this.extracting);
            if (cache != null) {
                IItemHandler extractingHandler = cache.getCapability();
                if (extractingHandler != null) {
                    for (int i = 0; i < extractingHandler.getSlots(); i++) {
                        ItemStack extracted = extractingHandler.extractItem(i, 32, false);
                        if (!extracted.isEmpty()) {
                            ItemStack remainder = itemHandler.insertItem(0, extracted, false);
                            if (!remainder.isEmpty()) {
                                extractingHandler.insertItem(i, remainder, false);
                            }
                            this.setFrom(this.extracting);
                            break;
                        }
                    }
                }
            }
        }

        // Fast path: skip if pipe has no item and isn't mid-movement
        if (!active && this.movement <= 0f && this.itemHandler.getStackInSlot(0).isEmpty()) return;

        // --- Move item toward the center (entry phase: -0.5 -> 0.0) ---
        if (this.movement < 0f) {
            this.lastMovement = this.movement;
            this.movement += transferSpeed;
            sendToTracking(new SyncPipeMovementPayload(getBlockPos(), this.movement, this.lastMovement));
            return; // Still approaching center, don't try to output yet
        }

        // Movement logic
        if (this.movement >= 1f) {
            ItemStack stack = itemHandler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                List<Direction> filteredOutputs = getFilteredOutputs(stack);
                Direction bestOutput = chooseBestOutput(filteredOutputs, stack);

                if (bestOutput != null) {
                    ItemPipeBE nextPipe = BlockUtils.getBE(ItemPipeBE.class, level, worldPosition.relative(bestOutput));

                    if (nextPipe != null && nextPipe.itemHandler.getStackInSlot(0).isEmpty()) {
                        // Smooth handoff to next pipe
                        nextPipe.itemHandler.setStackInSlot(0, stack.copy());
                        itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                        nextPipe.setFrom(bestOutput.getOpposite());

                        List<Direction> nextOutputs = nextPipe.getValidOutputs();
                        Direction nextTo = nextPipe.chooseBestOutput(nextOutputs, stack);
                        nextPipe.setTo(nextTo != null ? nextTo : nextPipe.from);

                        nextPipe.lastMovement = -0.5f;
                        nextPipe.movement = -0.5f;
                        this.active = false;

                        nextPipe.sendToTracking(new SyncPipeMovementPayload(nextPipe.getBlockPos(), nextPipe.movement, nextPipe.lastMovement));
                        nextPipe.sendToTracking(new SyncPipeDirectionPayload(nextPipe.getBlockPos(),
                                Optional.ofNullable(nextPipe.from), Optional.ofNullable(nextPipe.to)));
                    } else {
                        IItemHandler targetHandler = capabilityCaches.get(bestOutput).getCapability();
                        if (targetHandler != null) {
                            ItemStack remainder = ItemHandlerHelper.insertItem(targetHandler, stack, false);
                            if (remainder.isEmpty()) {
                                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                                this.active = false;
                            } else {
                                itemHandler.setStackInSlot(0, remainder);
                                moveItemBackward();
                            }
                        }
                    }
                } else {
                    moveItemBackward();
                }
            }
            this.lastMovement = 0;
            this.movement = 0;
        }

        // Advance movement (only when movement is between 0 and 1)
        if (this.movement >= 0f && this.movement < 1f) {
            this.lastMovement = this.movement;
            if (!this.itemHandler.getStackInSlot(0).isEmpty()) {
                this.movement += transferSpeed;
            } else {
                this.movement = 0;
            }
            sendToTracking(new SyncPipeMovementPayload(getBlockPos(), this.movement, this.lastMovement));
        }
    }

    /**
     * Get output directions filtered by the diamond pipe's filters.
     * A side is valid if:
     * - It has no filter items (accept all)
     * - OR the item matches at least one filter on that side
     */
    private List<Direction> getFilteredOutputs(ItemStack stack) {
        List<Direction> outputs = new ArrayList<>();
        Direction inputDir = this.from;

        for (Direction dir : directions) {
            if (dir == inputDir) continue;
            BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(dir);
            if (cache == null || cache.getCapability() == null) continue;

            // Check if this side's filter allows the item
            if (isFilterMatch(dir, stack)) {
                outputs.add(dir);
            }
        }
        return outputs;
    }

    /**
     * Check if an item matches the filter for a given side.
     * Empty filter = accept all.
     */
    private boolean isFilterMatch(Direction side, ItemStack stack) {
        int offset = side.ordinal() * SLOTS_PER_SIDE;
        boolean hasFilter = false;

        for (int i = 0; i < SLOTS_PER_SIDE; i++) {
            ItemStack filter = filterHandler.getStackInSlot(offset + i);
            if (!filter.isEmpty()) {
                hasFilter = true;
                if (ItemStack.isSameItemSameComponents(filter, stack)) {
                    return true;
                }
            }
        }

        // No filters set = accept all items
        return !hasFilter;
    }

    public ItemStackHandler getFilterHandler() {
        return filterHandler;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.buildcraft.diamond_pipe");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DiamondPipeMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("filters")) {
            filterHandler.deserializeNBT(registries, tag.getCompound("filters"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("filters", filterHandler.serializeNBT(registries));
    }
}
