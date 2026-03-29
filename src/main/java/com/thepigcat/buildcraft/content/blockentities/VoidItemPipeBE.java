package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.api.blockentities.PipeBlockEntity;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Void pipe - destroys any items that enter it.
 * Items are simply deleted instead of being bounced back.
 */
public class VoidItemPipeBE extends PipeBlockEntity<IItemHandler> {
    private final ItemStackHandler itemHandler;

    public VoidItemPipeBE(BlockPos pos, BlockState blockState) {
        this(BCBlockEntities.VOID_ITEM_PIPE.get(), pos, blockState);
    }

    public VoidItemPipeBE(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.itemHandler = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Override
    protected BlockCapability<IItemHandler, Direction> getCapType() {
        return Capabilities.ItemHandler.BLOCK;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            // Destroy any item that enters the void pipe
            if (!itemHandler.getStackInSlot(0).isEmpty()) {
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                setChanged();
            }
        }
    }

    public IItemHandler getItemHandler(Direction direction) {
        return itemHandler;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
