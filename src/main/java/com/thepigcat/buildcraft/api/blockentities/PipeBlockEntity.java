package com.thepigcat.buildcraft.api.blockentities;

import com.thepigcat.buildcraft.api.blocks.PipeBlock;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public abstract class PipeBlockEntity<CAP> extends BlockEntity {
    public Direction extracting;

    protected final Map<Direction, BlockCapabilityCache<CAP, Direction>> capabilityCaches;
    protected Set<Direction> directions;

    /** Whether this pipe has work to do (dirty flag to skip tick when idle) */
    public boolean active;

    /** Cached transfer speed (blocks per tick). Set by subclass via onLoad/init. */
    protected float transferSpeed;

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.capabilityCaches = new HashMap<>(6);
        this.directions = new ObjectArraySet<>(6);
    }

    protected abstract BlockCapability<CAP, Direction> getCapType();

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            PipeBlock.setPipeProperties(this);
            Direction[] directions = Direction.values();
            for (Direction direction : directions) {
                capabilityCaches.put(direction, BlockCapabilityCache.create(getCapType(), serverLevel, worldPosition.relative(direction), direction.getOpposite()));
            }
        }
    }

    public void tick() {}

    public void setDirections(Set<Direction> directions) {
        this.directions = directions;
    }

    public Set<Direction> getDirections() {
        return directions;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
    }

    /**
     * Send a packet only to players tracking this chunk, instead of broadcast to all.
     * Falls back to sendToAllPlayers if not a ServerLevel.
     */
    protected void sendToTracking(Object payload) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel,
                    new net.minecraft.world.level.ChunkPos(worldPosition), (net.minecraft.network.protocol.common.custom.CustomPacketPayload) payload);
        }
    }
}
