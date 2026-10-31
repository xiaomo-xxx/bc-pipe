package com.thepigcat.buildcraft.api.blockentities;

import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.api.pipes.MovementSyncTarget;
import com.thepigcat.buildcraft.networking.PipeSyncManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 会「运输东西」的管道方块实体的公共基类。
 * <p>
 * 物品管道和液体管道原本各写了一份完全相同的运输状态机外壳 —— movement 推进、from/to、
 * 客户端平滑、批量同步、存档。这里把这一层抽出来，子类只需要关心：
 * <ul>
 *     <li>自己装的是什么（物品槽 / 液体罐）</li>
 *     <li>怎么判断对方能不能收</li>
 *     <li>怎么把东西推出去</li>
 * </ul>
 * 运输进度本身统一用 {@link #movement} 表示：-0.5 入口面 → 0 中心 → 1.0 出口面。
 *
 * @param <CAP> 这个管道对外暴露的能力类型（IItemHandler / IFluidHandler）
 */
public abstract class AbstractTransportPipeBE<CAP> extends PipeBlockEntity<CAP> implements MovementSyncTarget {
    protected Direction from;
    protected Direction to;

    /**
     * 上一次成功输出的方向。物品流向在实践中很稳定，缓存下来可以让热路径少扫一遍所有输出。
     */
    protected Direction preferredOutput;

    /** 运输进度：-0.5 入口面 → 0 中心 → 1.0 出口面。 */
    public float movement;
    /** 上一 tick 的进度，用于客户端插值。 */
    public float lastMovement;

    /**
     * 客户端专用：服务端最近一次同步过来的目标进度。
     * <p>
     * 网络包到达的时刻并不均匀（服务端 tick 抖动 + 网络抖动），如果直接拿包里的值渲染，
     * 位置会每帧跳一下，看起来就是「抽搐」。客户端每 tick 只朝这个目标逼近一部分，
     * 运动就始终连续。
     */
    public float targetMovement;

    protected AbstractTransportPipeBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            cacheTransferSpeed();
        }
    }

    /**
     * 只在加载时算一次速度，避免每 tick 去查注册表。
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
        // transferSpeed = 每 tick 走的格数（服务端 20 TPS）
        this.transferSpeed = (float) (blocksPerSecond / 20.0);
    }

    @Override
    public void applyTargetMovement(float movement) {
        this.targetMovement = movement;
    }

    /**
     * 客户端每 tick 把 movement 朝服务端目标值逼近一半，保证渲染位置连续。
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
     * 把本 tick 的进度交给 {@link PipeSyncManager}，按区块合并成每个区块一个包。
     * <p>
     * public 是因为子类在别的包里，搬运交接时需要给「下一根管道」同步一次。
     */
    public void queueMovementSync() {
        if (level instanceof ServerLevel serverLevel) {
            PipeSyncManager.queueMovement(serverLevel, worldPosition, this.movement);
        }
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

    /**
     * 换向回弹：出入口对调，进度归零。
     * <p>
     * 只做状态变更和进度同步；需要额外把方向同步给客户端的子类（比如物品管道要渲染在途物品）
     * 覆写后自行发包。
     */
    protected void moveBackward() {
        Direction oldTo = this.to;
        this.to = this.from;
        this.from = oldTo;
        this.lastMovement = 0f;
        this.movement = 0f;
        queueMovementSync();
    }

    protected void saveTransport(CompoundTag tag) {
        tag.putInt("to", to != null ? to.ordinal() : -1);
        tag.putInt("from", from != null ? from.ordinal() : -1);
        tag.putFloat("movement", movement);
        tag.putFloat("lastMovement", lastMovement);
    }

    protected void loadTransport(CompoundTag tag) {
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
}
