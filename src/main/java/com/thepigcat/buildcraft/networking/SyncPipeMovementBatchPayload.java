package com.thepigcat.buildcraft.networking;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.content.blockentities.ItemPipeBE;
import com.thepigcat.buildcraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Batched replacement for the old per-pipe {@code SyncPipeMovementPayload}.
 * <p>
 * A single pipe network easily spans dozens of block entities; sending one packet per pipe per
 * sync tick meant the server was doing a chunk-tracking lookup plus a packet allocation for every
 * single pipe. This payload collapses every dirty pipe of one chunk into one packet, which is
 * flushed once per server tick by {@link PipeSyncManager}.
 */
public record SyncPipeMovementBatchPayload(ChunkPos chunkPos, List<Entry> entries) implements CustomPacketPayload {
    public static final Type<SyncPipeMovementBatchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BuildcraftLegacy.MODID, "sync_pipe_movement_batch"));

    /**
     * Implemented as an anonymous StreamCodec on purpose: the payload is hand-rolled instead of
     * composite-encoded because the entry list is variable length and packed tighter this way
     * (4 bytes of floats per pipe instead of a full BlockPos + two boxed floats).
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPipeMovementBatchPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, SyncPipeMovementBatchPayload payload) {
                    buf.writeVarInt(payload.chunkPos().x);
                    buf.writeVarInt(payload.chunkPos().z);
                    buf.writeVarInt(payload.entries().size());
                    for (Entry entry : payload.entries()) {
                        BlockPos.STREAM_CODEC.encode(buf, entry.pos());
                        buf.writeFloat(entry.movement());
                    }
                }

                @Override
                public SyncPipeMovementBatchPayload decode(RegistryFriendlyByteBuf buf) {
                    int chunkX = buf.readVarInt();
                    int chunkZ = buf.readVarInt();
                    int size = buf.readVarInt();
                    List<Entry> entries = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                        entries.add(new Entry(pos, buf.readFloat()));
                    }
                    return new SyncPipeMovementBatchPayload(new ChunkPos(chunkX, chunkZ), entries);
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPipeMovementBatchPayload payload, IPayloadContext context) {
        // All entries belong to the same chunk, so resolve the level once and reuse it.
        context.enqueueWork(() -> {
            Level level = context.player().level();
            for (Entry entry : payload.entries()) {
                ItemPipeBE be = BlockUtils.getBE(ItemPipeBE.class, level, entry.pos());
                if (be != null) {
                    // 只写目标值，实际渲染用的 movement 由 ItemPipeBE#tickClient 平滑逼近，
                    // 避免数据包到达时刻不均导致的抖动。
                    be.targetMovement = entry.movement();
                }
            }
        });
    }

    public record Entry(BlockPos pos, float movement) {
    }
}
