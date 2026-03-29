package com.thepigcat.buildcraft.content.blocks;

import com.mojang.serialization.MapCodec;
import com.thepigcat.buildcraft.PipesRegistry;
import com.thepigcat.buildcraft.api.blockentities.PipeBlockEntity;
import com.thepigcat.buildcraft.api.blocks.PipeBlock;
import com.thepigcat.buildcraft.api.pipes.Pipe;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.util.CapabilityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Void pipe block - destroys items that enter it.
 */
public class VoidItemPipeBlock extends PipeBlock {
    public VoidItemPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public PipeState getConnectionType(LevelAccessor level, BlockPos pipePos, BlockState pipeState, Direction connectionDirection, BlockPos connectPos) {
        BlockEntity be = level.getBlockEntity(connectPos);
        if (be != null && CapabilityUtils.itemHandlerCapability(be, connectionDirection.getOpposite()) != null) {
            return PipeState.CONNECTED;
        }
        return PipeState.NONE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(VoidItemPipeBlock::new);
    }

    @Override
    protected BlockEntityType<? extends PipeBlockEntity<?>> getBlockEntityType() {
        return BCBlockEntities.VOID_ITEM_PIPE.get();
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Pipe pipe = PipesRegistry.PIPES.get(this.builtInRegistryHolder().key().location().getPath());
        Item dropItem = BuiltInRegistries.ITEM.get(pipe.dropItem());
        if (!pipe.customLoottable()) {
            return List.of(dropItem.getDefaultInstance());
        }
        return super.getDrops(state, params);
    }
}
