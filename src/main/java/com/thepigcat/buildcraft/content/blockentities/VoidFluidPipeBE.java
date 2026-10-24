package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.registries.BCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * 虚空液体管道：进来的液体直接销毁。
 */
public class VoidFluidPipeBE extends FluidPipeBE {
    public VoidFluidPipeBE(BlockPos pos, BlockState state) {
        super(BCBlockEntities.VOID_FLUID_PIPE.get(), pos, state);
    }

    @Override
    public void tick() {
        if (level.isClientSide()) {
            tickClient();
            return;
        }

        if (!this.tank.isEmpty()) {
            this.tank.drain(this.tank.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
            this.active = false;
        }
    }
}
