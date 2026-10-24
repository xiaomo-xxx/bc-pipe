package com.thepigcat.buildcraft.content.blockentities;

import com.google.common.collect.ImmutableMap;
import com.portingdeadmods.portingdeadlibs.api.blockentities.ContainerBlockEntity;
import com.portingdeadmods.portingdeadlibs.api.capabilities.DynamicFluidTank;
import com.portingdeadmods.portingdeadlibs.api.utils.IOAction;
import com.portingdeadmods.portingdeadlibs.utils.capabilities.HandlerUtils;
import com.portingdeadmods.portingdeadlibs.utils.capabilities.SidedCapUtils;
import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.data.BCDataComponents;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.util.BlockUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TankBE extends ContainerBlockEntity {
    private BlockPos bottomTankPos;
    private boolean topJoined;
    private boolean bottomJoined;
    public FluidStack initialFluid;

    /**
     * 破坏时预先算好的掉落流体。
     * <p>
     * 关键：{@code onRemove}（拆分/上移/扣除流体）先于 {@code getDrops} 执行，
     * 等 getDrops 再去读罐子时，里面的流体已经被重分配掉了，所以必须在改动之前先存一份。
     */
    public FluidStack pendingDropFluid = FluidStack.EMPTY;

    public TankBE(BlockPos pos, BlockState blockState) {
        super(BCBlockEntities.TANK.get(), pos, blockState);
        addFluidHandler(HandlerUtils::newDynamicFluidTank, builder -> builder
                .onChange($ -> this.updateData())
                .slotLimit($ -> BCConfig.tankCapacity));
    }

    public FluidStack getFluid() {
        return getFluidHandler().getFluidInTank(0);
    }

    public DynamicFluidTank getFluidTank() {
        return ((DynamicFluidTank) super.getFluidHandler());
    }

    @Override
    public IFluidHandler getFluidHandler() {
        if (this.bottomTankPos != null) {
            if (this.bottomTankPos.equals(worldPosition)) {
                return (this.getFluidTank());
            } else {
                TankBE tankBE = BlockUtils.getBE(TankBE.class, level, this.bottomTankPos);
                if (tankBE != null) {
                    return (tankBE.getFluidHandler());
                } else {
                    return this.getFluidTank();
                }
            }
        } else {
            return this.getFluidTank();
        }
    }

    public void setBottomTankPos(BlockPos bottomTankPos) {
        this.bottomTankPos = bottomTankPos;
        this.updateData();
    }

    public void initTank(int tanks) {
        this.getFluidTank().setCapacity(tanks * BCConfig.tankCapacity);
        if (this.initialFluid != null) {
            this.getFluidTank().setFluid(this.initialFluid);
            this.initialFluid = null;
        }
        this.updateData();
    }

    public BlockPos getBottomTankPos() {
        return bottomTankPos;
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadData(tag, provider);
        this.bottomTankPos = BlockPos.of(tag.getLong("bottomTankPos"));
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveData(tag, provider);
        if (this.bottomTankPos != null) {
            tag.putLong("bottomTankPos", this.bottomTankPos.asLong());
        }
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        super.saveToItem(stack, registries);

        stack.set(BCDataComponents.TANK_CONTENT.get(), SimpleFluidContent.copyOf(this.getFluidTank().getFluid()));
    }

    public void setTopJoined(boolean topJoined) {
        this.topJoined = topJoined;
    }

    public void setBottomJoined(boolean bottomJoined) {
        this.bottomJoined = bottomJoined;
    }

    public boolean isTopJoined() {
        return topJoined;
    }

    public boolean isBottomJoined() {
        return bottomJoined;
    }

}
