package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.registries.BCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 抽取型液体管道（木质）。抽取逻辑在 {@link FluidPipeBE#tryExtract()} 里，这里只是绑定方块实体类型。
 */
public class ExtractingFluidPipeBE extends FluidPipeBE {
    public ExtractingFluidPipeBE(BlockPos pos, BlockState state) {
        super(BCBlockEntities.EXTRACTING_FLUID_PIPE.get(), pos, state);
    }
}
