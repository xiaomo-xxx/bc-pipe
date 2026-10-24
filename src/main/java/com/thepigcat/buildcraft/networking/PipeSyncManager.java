package com.thepigcat.buildcraft.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Coalesces pipe movement updates into one packet per chunk per server tick.
 * <p>
 * Before this, every pipe called {@code PacketDistributor.sendToPlayersTrackingChunk} on its own
 * (every 2 ticks for normal pipes, <b>every tick</b> for diamond pipes). A 200 pipe network was
 * therefore issuing 100-200 chunk-tracking lookups and packet allocations per tick, each carrying
 * a single 12 byte payload. Batching turns that into one packet per chunk that actually has
 * moving pipes, and lets us sync every tick instead of every other tick, so the animation is both
 * cheaper and smoother.
 */
public final class PipeSyncManager {
    /**
     * ServerLevel is used as a weak key so a level that unloads (or a server that stops) does not
     * keep its pending updates alive. Everything here runs on the server thread only.
     */
    private static final Map<ServerLevel, Map<ChunkPos, Map<BlockPos, SyncPipeMovementBatchPayload.Entry>>> PENDING =
            new WeakHashMap<>();

    private PipeSyncManager() {
    }

    /**
     * Queues a movement update. Calling this more than once for the same pipe in a single tick is
     * harmless - the map entry is overwritten, which also deduplicates the two updates a diamond
     * pipe produces when it finishes a segment.
     */
    public static void queueMovement(ServerLevel level, BlockPos pos, float movement, float lastMovement) {
        Map<ChunkPos, Map<BlockPos, SyncPipeMovementBatchPayload.Entry>> perChunk =
                PENDING.computeIfAbsent(level, l -> new HashMap<>());

        ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        BlockPos key = pos.immutable();

        perChunk.computeIfAbsent(chunkPos, k -> new HashMap<>())
                .put(key, new SyncPipeMovementBatchPayload.Entry(key, movement, lastMovement));
    }

    /**
     * Sends one packet for every chunk with pending updates and clears the queue.
     */
    public static void flush(ServerLevel level) {
        Map<ChunkPos, Map<BlockPos, SyncPipeMovementBatchPayload.Entry>> perChunk = PENDING.get(level);
        if (perChunk == null || perChunk.isEmpty()) {
            return;
        }

        for (Map.Entry<ChunkPos, Map<BlockPos, SyncPipeMovementBatchPayload.Entry>> chunkEntry : perChunk.entrySet()) {
            ChunkPos chunkPos = chunkEntry.getKey();
            List<SyncPipeMovementBatchPayload.Entry> entries = new ArrayList<>(chunkEntry.getValue().values());
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos,
                    new SyncPipeMovementBatchPayload(chunkPos, entries));
        }

        perChunk.clear();
    }

    /**
     * Drops every queued update for a level, used when a level goes away.
     */
    public static void clear(ServerLevel level) {
        PENDING.remove(level);
    }
}
