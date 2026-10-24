package com.thepigcat.buildcraft.content.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.portingdeadlibs.api.blockentities.ContainerBlockEntity;
import com.portingdeadmods.portingdeadlibs.api.blocks.ContainerBlock;
import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.content.blockentities.TankBE;
import com.thepigcat.buildcraft.data.BCDataComponents;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.util.BlockUtils;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TankBlock extends ContainerBlock {
    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    public static final BooleanProperty TOP_JOINED = BooleanProperty.create("top_joined");
    public static final BooleanProperty BOTTOM_JOINED = BooleanProperty.create("bottom_joined");

    public TankBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TOP_JOINED, false).setValue(BOTTOM_JOINED, false));
    }

    @Override
    public boolean tickingEnabled() {
        return false;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return BCBlockEntities.TANK.get();
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TankBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(TOP_JOINED, BOTTOM_JOINED));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        TankBE be = BlockUtils.getBE(TankBE.class, level, pos);
        IFluidHandler itemFluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        IFluidHandler tankFluidHandler = be.getFluidHandler();

        if (itemFluidHandler != null && !(stack.getItem() instanceof BucketItem)) {
            FluidStack fluidInItemTank = itemFluidHandler.getFluidInTank(0);
            IFluidHandler fluidHandler0 = tankFluidHandler;
            IFluidHandler fluidHandler1 = itemFluidHandler;

            if (!fluidInItemTank.isEmpty()) {
                fluidInItemTank.getFluid().getPickupSound().ifPresent(player::playSound);
                fluidHandler0 = itemFluidHandler;
                fluidHandler1 = tankFluidHandler;
            } else {
                SoundEvent sound = tankFluidHandler.getFluidInTank(0).getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                if (sound != null) {
                    player.playSound(sound);
                }
            }

            FluidStack drained = fluidHandler0.drain(fluidHandler0.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
            int filled = fluidHandler1.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            fluidHandler0.fill(drained.copyWithAmount(drained.getAmount() - filled), IFluidHandler.FluidAction.EXECUTE);

            return ItemInteractionResult.SUCCESS;
        } else if (itemFluidHandler != null && stack.getItem() instanceof BucketItem) {
            FluidStack fluidInItemTank = itemFluidHandler.getFluidInTank(0);
            if (fluidInItemTank.isEmpty() && tankFluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE).getAmount() == 1000) {
                ItemStack filledBucket = ItemUtils.createFilledResult(stack, player, tankFluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE).getFluid().getBucket().getDefaultInstance());
                player.setItemInHand(hand, filledBucket);
                tankFluidHandler.getFluidInTank(0).getFluid().getPickupSound().ifPresent(player::playSound);
                return ItemInteractionResult.SUCCESS;
            } else if (!fluidInItemTank.isEmpty() && tankFluidHandler.fill(fluidInItemTank.copyWithAmount(1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                tankFluidHandler.fill(fluidInItemTank.copyWithAmount(1000), IFluidHandler.FluidAction.EXECUTE);
                ItemStack emptyBucket = ItemUtils.createFilledResult(stack, player, BucketItem.getEmptySuccessItem(stack, player));
                player.setItemInHand(hand, emptyBucket);
                SoundEvent sound = tankFluidHandler.getFluidInTank(0).getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                if (sound != null) {
                    player.playSound(sound);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        FluidStack itemTankFluid = context.getItemInHand().getOrDefault(BCDataComponents.TANK_CONTENT, SimpleFluidContent.EMPTY).copy();
        boolean topJoined = level.getBlockEntity(clickedPos.above()) instanceof TankBE tankBE && (tankBE.getFluid().is(itemTankFluid.getFluid()) || tankBE.getFluid().isEmpty() || itemTankFluid.isEmpty());
        boolean bottomJoined = level.getBlockEntity(clickedPos.below()) instanceof TankBE tankBE && (tankBE.getFluid().is(itemTankFluid.getFluid()) || tankBE.getFluid().isEmpty() || itemTankFluid.isEmpty());

        if (topJoined && bottomJoined) {
            TankBE aboveTank = BlockUtils.getBE(TankBE.class, level, clickedPos.above());
            TankBE belowTank = BlockUtils.getBE(TankBE.class, level, clickedPos.below());
            if (!aboveTank.getFluid().is(belowTank.getFluid().getFluid()) || !aboveTank.getFluid().is(itemTankFluid.getFluid())) {
                topJoined = false;
            }

        }

        return state != null ? state.setValue(TOP_JOINED, topJoined).setValue(BOTTOM_JOINED, bottomJoined) : null;
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        TankBE tankBE = BlockUtils.getBE(TankBE.class, level, pos);
        if (tankBE == null) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
        FluidStack fluidInTank = tankBE.getFluidHandler().getFluidInTank(0);
        boolean value = neighborState.is(this);
        if (direction == Direction.UP) {
            if (value) {
                TankBE aboveBE = BlockUtils.getBE(TankBE.class, level, pos.above());
                if (aboveBE != null) {
                    FluidStack fluidInTank1 = aboveBE.getFluidHandler().getFluidInTank(0);
                    value = fluidInTank1.is(fluidInTank.getFluid()) || fluidInTank.isEmpty() || fluidInTank1.isEmpty();
                } else {
                    value = false;
                }
            }
            tankBE.setTopJoined(value);
            return state.setValue(TOP_JOINED, value);
        } else if (direction == Direction.DOWN) {
            if (value) {
                TankBE belowBE = BlockUtils.getBE(TankBE.class, level, pos.below());
                if (belowBE != null) {
                    FluidStack fluidInTank1 = belowBE.getFluidHandler().getFluidInTank(0);
                    value = fluidInTank1.is(fluidInTank.getFluid()) || fluidInTank.isEmpty() || fluidInTank1.isEmpty();
                } else {
                    value = false;
                }
            }
            tankBE.setBottomJoined(value);
            return state.setValue(BOTTOM_JOINED, value);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        int aboveFluidAmount = 0;
        if (state.getValue(TOP_JOINED)) {
            TankBE aboveTank = BlockUtils.getBE(TankBE.class, level, pos.above());
            aboveFluidAmount = aboveTank.getFluidHandler().getFluidInTank(0).getAmount();
        }

        TankBE tankBE = BlockUtils.getBE(TankBE.class, level, pos);
        FluidStack baseFluidCopy = tankBE.getFluidTank().getFluid().copy();
        int baseFluidAmount = tankBE.getFluidTank().getFluid().getAmount();
        tankBE.setTopJoined(state.getValue(TOP_JOINED));
        tankBE.setBottomJoined(state.getValue(BOTTOM_JOINED));
        if (!state.getValue(BOTTOM_JOINED) && !state.getValue(TOP_JOINED)) {
            tankBE.setBottomTankPos(pos);
            tankBE.initTank(1);
            return;
        }

        BlockPos bottomPos = pos;
        if (state.getValue(BOTTOM_JOINED)) {
            while (level.getBlockState(bottomPos).getValue(BOTTOM_JOINED)) {
                bottomPos = bottomPos.below();
            }
        }

        BlockPos curPos = bottomPos;
        while (level.getBlockState(curPos).getValue(TOP_JOINED)) {
            BlockUtils.getBE(TankBE.class, level, curPos).setBottomTankPos(bottomPos);
            curPos = curPos.above();
        }
        BlockUtils.getBE(TankBE.class, level, curPos).setBottomTankPos(bottomPos);
        BlockPos topPos = curPos;
        int yDiff = topPos.getY() - bottomPos.getY();

        TankBE bottomTankBe = BlockUtils.getBE(TankBE.class, level, bottomPos);
        if (!state.is(oldState.getBlock()) && state.getValue(TOP_JOINED) && state.getValue(BOTTOM_JOINED)) {
            FluidStack fluidInTank = bottomTankBe.getFluidHandler().getFluidInTank(0);
            int amount = fluidInTank.getAmount();
            if (fluidInTank.isEmpty()) {
                fluidInTank = baseFluidCopy;
            }
            bottomTankBe.initialFluid = fluidInTank.copyWithAmount(amount + aboveFluidAmount + baseFluidAmount);
        } else if (!state.is(oldState.getBlock()) && state.getValue(TOP_JOINED) && !state.getValue(BOTTOM_JOINED)) {
            FluidStack fluidInTank = BlockUtils.getBE(TankBE.class, level, pos.above()).getFluidTank().getFluidInTank(0);
            int amount = fluidInTank.getAmount();
            if (fluidInTank.isEmpty()) {
                fluidInTank = baseFluidCopy;
            }
            bottomTankBe.initialFluid = fluidInTank.copyWithAmount(amount + baseFluidAmount);
        } else if (!state.is(oldState.getBlock()) && !state.getValue(TOP_JOINED) && state.getValue(BOTTOM_JOINED)) {
            FluidStack fluidInTank = bottomTankBe.getFluidTank().getFluidInTank(0);
            int amount = fluidInTank.getAmount();
            if (fluidInTank.isEmpty()) {
                fluidInTank = baseFluidCopy;
            }
            bottomTankBe.initialFluid = fluidInTank.copyWithAmount(amount + baseFluidAmount);
        }

        bottomTankBe.initTank(yDiff + 1);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            // 必须在拆分/重分配之前把这一格该掉的流体存下来，
            // 否则 getDrops 晚一步执行，读到的已经是改动后的流体。
            TankBE removedBE = BlockUtils.getBE(TankBE.class, level, pos);
            if (removedBE != null) {
                removedBE.pendingDropFluid = computeDropFluid(removedBE);
            }

            if (state.getValue(TOP_JOINED) && state.getValue(BOTTOM_JOINED)) {
                splitTank(level, pos);
            } else if (state.getValue(TOP_JOINED) && !state.getValue(BOTTOM_JOINED)) {
                moveFluidsAbove(level, pos);
            } else if (!state.getValue(TOP_JOINED) && state.getValue(BOTTOM_JOINED)) {
                removeFluidFromBottomTank(level, pos);
            }
        }
        // Skip ContainerBlock.onRemove to avoid PDL framework's dropItems on fluid handler
        // The getDrops method handles saving tank contents via data components
        // Manually remove block entity (what Block.onRemove does internally)
        if (!state.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
    }

    /**
     * 计算破坏这一格储罐时应当掉落多少流体。
     * <p>
     * 堆叠储罐的流体全部存在最底部那一格，所以第 n 格对应的区间是
     * {@code [n * capacity, (n+1) * capacity)}，只有落在区间里的部分才属于这一格。
     */
    private static FluidStack computeDropFluid(TankBE be) {
        if (!BCConfig.tankRetainFluids) {
            return FluidStack.EMPTY;
        }

        FluidStack total = be.getFluidHandler().getFluidInTank(0);
        if (total.isEmpty() || total.getAmount() <= 0) {
            return FluidStack.EMPTY;
        }

        BlockPos bottomPos = be.getBottomTankPos();
        int index = bottomPos == null ? 0 : Math.max(be.getBlockPos().getY() - bottomPos.getY(), 0);
        int below = index * BCConfig.tankCapacity;
        int amount = Math.min(Math.max(total.getAmount() - below, 0), BCConfig.tankCapacity);

        return amount > 0 ? total.copyWithAmount(amount) : FluidStack.EMPTY;
    }

    private static void removeFluidFromBottomTank(Level level, BlockPos pos) {
        TankBE removedTank = BlockUtils.getBE(TankBE.class, level, pos);
        if (removedTank == null || removedTank.getBottomTankPos() == null) return;
        FluidStack fluidStack = removedTank.getFluidHandler().getFluidInTank(0);
        int tank = removedTank.getBlockPos().getY() - removedTank.getBottomTankPos().getY();
        int prevFluidAmount = tank * BCConfig.tankCapacity;
        int fluidAmount = Math.min(fluidStack.getAmount() - prevFluidAmount, BCConfig.tankCapacity);
        if (fluidAmount > 0) {
            removedTank.getFluidHandler().drain(fluidAmount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private static void moveFluidsAbove(Level level, BlockPos pos) {
        TankBE removedTank = BlockUtils.getBE(TankBE.class, level, pos);
        TankBE aboveTank = BlockUtils.getBE(TankBE.class, level, pos.above());
        if (removedTank == null || aboveTank == null) return;
        FluidStack fluidInTank = removedTank.getFluidTank().getFluidInTank(0);
        int amount = Math.max(fluidInTank.getAmount() - BCConfig.tankCapacity, 0);
        aboveTank.initialFluid = fluidInTank.copyWithAmount(amount);
    }

    private static void splitTank(Level level, BlockPos pos) {
        TankBE removedTank = BlockUtils.getBE(TankBE.class, level, pos);
        if (removedTank == null || removedTank.getBottomTankPos() == null) return;

        FluidStack fluidStack = removedTank.getFluidHandler().getFluidInTank(0);
        int tank = removedTank.getBlockPos().getY() - removedTank.getBottomTankPos().getY();

        TankBE topTank = BlockUtils.getBE(TankBE.class, level, pos.above());
        TankBE bottomTank = BlockUtils.getBE(TankBE.class, level, removedTank.getBottomTankPos());
        if (topTank == null || bottomTank == null) return;

        int topFluidAmount = Math.max(fluidStack.getAmount() - ((tank + 1) * BCConfig.tankCapacity), 0);
        topTank.initialFluid = fluidStack.copyWithAmount(topFluidAmount);

        int prevFluidAmount = tank * BCConfig.tankCapacity;
        int fluidAmount = Math.min(fluidStack.getAmount() - prevFluidAmount, BCConfig.tankCapacity);

        bottomTank.initialFluid = fluidStack.copyWithAmount(Math.max(fluidStack.getAmount() - topFluidAmount - fluidAmount, 0));
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TankBE be)) {
            return super.getDrops(state, params);
        }

        ItemStack stack = new ItemStack(this);
        if (BCConfig.tankRetainFluids) {
            // 正常流程里 onRemove 已经算好了；万一没走 onRemove，这里兜底再算一次
            FluidStack fluid = be.pendingDropFluid;
            if (fluid.isEmpty()) {
                fluid = computeDropFluid(be);
            }
            if (!fluid.isEmpty() && fluid.getAmount() > 0) {
                stack.set(BCDataComponents.TANK_CONTENT, SimpleFluidContent.copyOf(fluid));
            }
        }
        return List.of(stack);
    }

}
