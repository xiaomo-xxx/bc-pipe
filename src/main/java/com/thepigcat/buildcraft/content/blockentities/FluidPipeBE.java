package com.thepigcat.buildcraft.content.blockentities;

import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.api.blockentities.PipeBlockEntity;
import com.thepigcat.buildcraft.api.pipes.MovementSyncTarget;
import com.thepigcat.buildcraft.networking.PipeSyncManager;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;

/**
 * 液体管道。
 * <p>
 * 和物品管道一样用 movement（-0.5 入口面 → 1.0 出口面）表示一段管道里的运输进度，
 * 只是管道里装的是 {@link FluidTank} 而不是物品槽。抽取管道从相邻容器拉液体，
 * 普通管道靠上游灌入，到 1.0 时推给下一格。
 */
public class FluidPipeBE extends PipeBlockEntity<IFluidHandler> implements MovementSyncTarget {
    /** 每段管道一次能承载的液体量（mB）。相当于"一个液体包"。 */
    public static final int PIPE_CAPACITY = 1000;

    protected final FluidTank tank;

    protected Direction from;
    protected Direction to;
    private Direction preferredOutput;

    public float movement;
    public float lastMovement;
    public float targetMovement;

    public FluidPipeBE(BlockPos pos, BlockState state) {
        this(BCBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    public FluidPipeBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tank = new FluidTank(PIPE_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    active = !getFluid().isEmpty();
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    public FluidTank getTank() {
        return tank;
    }

    public IFluidHandler getFluidHandler(Direction direction) {
        return tank;
    }

    @Override
    protected BlockCapability<IFluidHandler, Direction> getCapType() {
        return Capabilities.FluidHandler.BLOCK;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            cacheTransferSpeed();
        }
    }

    /** 速度沿用物品管道的配置，按方块 id 匹配，只在加载时算一次。 */
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
        this.transferSpeed = (float) (blocksPerSecond / 20.0);
    }

    @Override
    public void applyTargetMovement(float movement) {
        this.targetMovement = movement;
    }

    @Override
    public void tick() {
        if (level.isClientSide()) {
            tickClient();
            return;
        }

        if (this.tank.isEmpty()) {
            tryExtract();
            if (this.tank.isEmpty()) {
                this.lastMovement = 0f;
                this.movement = 0f;
                return;
            }
        }

        // 入口段：从入口面走向中心
        if (this.movement < 0f) {
            this.lastMovement = this.movement;
            this.movement += transferSpeed;
            queueMovementSync();
            return;
        }

        // 到出口了，推给下一格
        if (this.movement >= 1f) {
            pushOut();
            this.movement = 0f;
            this.lastMovement = 0f;
            return;
        }

        this.lastMovement = this.movement;
        this.movement += transferSpeed;
        queueMovementSync();
    }

    protected void tickClient() {
        this.lastMovement = this.movement;

        float delta = this.targetMovement - this.movement;
        if (Math.abs(delta) < 0.002f || Math.abs(delta) > 0.6f) {
            this.movement = this.targetMovement;
        } else {
            this.movement += delta * 0.5f;
        }
    }

    protected void queueMovementSync() {
        if (level instanceof ServerLevel serverLevel) {
            PipeSyncManager.queueMovement(serverLevel, worldPosition, this.movement);
        }
    }

    /** 抽取管道用：从 extracting 那一侧的容器里抽一批液体。 */
    protected void tryExtract() {
        if (this.extracting == null) return;

        BlockCapabilityCache<IFluidHandler, Direction> cache = capabilityCaches.get(this.extracting);
        if (cache == null) return;
        IFluidHandler source = cache.getCapability();
        if (source == null) return;

        FluidStack drained = source.drain(PIPE_CAPACITY, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) return;

        int accepted = this.tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (accepted <= 0) return;

        source.drain(drained.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);

        this.setFrom(this.extracting);
        this.setTo(chooseOutput(getValidOutputs()));
    }

    protected void pushOut() {
        if (this.tank.isEmpty()) return;

        Direction out = chooseOutput(getValidOutputs());
        if (out == null) {
            moveBackward();
            return;
        }

        BlockCapabilityCache<IFluidHandler, Direction> cache = capabilityCaches.get(out);
        IFluidHandler target = cache == null ? null : cache.getCapability();
        if (target == null) {
            moveBackward();
            return;
        }

        int accepted = target.fill(this.tank.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
        if (accepted <= 0) {
            moveBackward();
            return;
        }
        this.tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);

        // 目标是另一根液体管道：让它从这一侧重新开始运输，接上视觉动线
        if (target instanceof FluidPipeBE next) {
            next.setFrom(out.getOpposite());
            next.setTo(next.chooseOutput(next.getValidOutputs()));
            next.lastMovement = -0.5f;
            next.movement = -0.5f;
            next.queueMovementSync();
        }
    }

    /** 排除来路，且目标当前确实能接收液体。 */
    protected List<Direction> getValidOutputs() {
        List<Direction> outputs = new ArrayList<>();
        if (this.tank.isEmpty()) return outputs;

        for (Direction dir : directions) {
            if (dir == this.from) continue;
            BlockCapabilityCache<IFluidHandler, Direction> cache = capabilityCaches.get(dir);
            if (cache == null) continue;
            IFluidHandler handler = cache.getCapability();
            if (handler == null) continue;
            // 用 1 mB 试灌一下，避免灌不进去还要来回弹
            if (handler.fill(this.tank.getFluid().copyWithAmount(1), IFluidHandler.FluidAction.SIMULATE) > 0) {
                outputs.add(dir);
            }
        }
        return outputs;
    }

    protected Direction chooseOutput(List<Direction> outputs) {
        if (outputs.isEmpty()) return null;
        if (outputs.size() == 1) {
            preferredOutput = outputs.getFirst();
            return preferredOutput;
        }
        if (preferredOutput != null && outputs.contains(preferredOutput)) {
            return preferredOutput;
        }
        preferredOutput = outputs.getFirst();
        return preferredOutput;
    }

    protected void moveBackward() {
        Direction oldTo = this.to;
        this.to = this.from;
        this.from = oldTo;
        this.movement = 0f;
        this.lastMovement = 0f;
        queueMovementSync();
    }

    public void setFrom(Direction from) {
        this.from = from;
    }

    public void setTo(Direction to) {
        this.to = to;
    }

    public Direction getFrom() {
        return from;
    }

    public Direction getTo() {
        return to;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.deserializeNBT(registries, tag.getCompound("tank"));
        int fromIndex = tag.getInt("from");
        if (fromIndex != -1) {
            this.from = Direction.values()[fromIndex];
        }
        int toIndex = tag.getInt("to");
        if (toIndex != -1) {
            this.to = Direction.values()[toIndex];
        }
        this.movement = tag.getFloat("movement");
        this.lastMovement = tag.getFloat("lastMovement");
        this.targetMovement = this.movement;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("tank", this.tank.serializeNBT(registries));
        tag.putInt("from", from != null ? from.ordinal() : -1);
        tag.putInt("to", to != null ? to.ordinal() : -1);
        tag.putFloat("movement", movement);
        tag.putFloat("lastMovement", lastMovement);
    }
}
