package com.thepigcat.buildcraft.api.pipes;

/**
 * 由需要把「运输进度」（物品/液体在管道里走到了哪儿）同步给客户端的方块实体实现。
 * <p>
 * 物品管道和液体管道共用 {@link com.thepigcat.buildcraft.networking.PipeSyncManager}
 * 的按区块批量同步，客户端只写目标值，实际渲染值由各自的 tickClient 平滑逼近。
 */
public interface MovementSyncTarget {
    void applyTargetMovement(float movement);
}
