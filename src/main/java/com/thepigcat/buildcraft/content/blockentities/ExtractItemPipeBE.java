package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.util.BlockUtils;
import com.thepigcat.buildcraft.networking.SyncPipeDirectionPayload;
import com.thepigcat.buildcraft.networking.SyncPipeMovementPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExtractItemPipeBE extends ItemPipeBE {
    public ExtractItemPipeBE(BlockPos pos, BlockState blockState) {
        super(BCBlockEntities.EXTRACTING_ITEM_PIPE.get(), pos, blockState);
    }

    @Override
    public void tick() {
        // Extract items from the source container
        if (!level.isClientSide() && level.getGameTime() % 10 == 0 && itemHandler.getStackInSlot(0).isEmpty()) {
            BlockCapabilityCache<IItemHandler, Direction> cache = capabilityCaches.get(this.extracting);
            if (cache != null) {
                IItemHandler extractingHandler = cache.getCapability();
                if (extractingHandler != null) {
                    // Find an item to extract
                    for (int i = 0; i < extractingHandler.getSlots(); i++) {
                        ItemStack extracted = extractingHandler.extractItem(i, 64, false);
                        if (!extracted.isEmpty()) {
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

                            PacketDistributor.sendToAllPlayers(new SyncPipeDirectionPayload(worldPosition,
                                    Optional.ofNullable(from), Optional.ofNullable(to)));
                            break;
                        }
                    }
                }
            }
        }

        // Regular pipe movement logic
        super.tick();
    }
}
