package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AxonBlockEntity extends BlockEntity {

    protected static final Codec<Map<AxonType, LocalAxonConnection>> UPSTREAM_CODEC = Codec.unboundedMap(AxonType.CODEC, LocalAxonConnection.CODEC);
    protected final @NotNull Map<AxonType, LocalAxonConnection> upstream = new Reference2ObjectOpenHashMap<>();
    protected static final Codec<Collection<BlockPos>> DOWNSTREAM_CODEC = Codec.list(BlockPos.CODEC).xmap(UnaryOperator.identity(), ObjectArrayList::new);
    protected final @NotNull Set<BlockPos> downstream = new ObjectOpenHashSet<>();

    protected final @NotNull ConnectionTier tier;

    public AxonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, @NotNull ConnectionTier tier) {
        super(type, pos, state);
        this.tier = tier;
    }

    public @NotNull ConnectionTier getTier() {
        return tier;
    }

    public boolean hasUpstream(@NotNull AxonType type) {
        return upstream.containsKey(type);
    }

    public @Nullable LocalAxonConnection setUpstream(@NotNull AxonType type, @NotNull LocalAxonConnection connection, boolean dropOld) {
        LocalAxonConnection prev = upstream.put(type, connection);
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof AxonBlockEntity a) a.addDownstream(getBlockPos());
            if (prev != null) {
                be = getLevel().getBlockEntity(prev.getTargetPos());
                if (be instanceof AxonBlockEntity a) a.removeDownstream(getBlockPos());
                if (dropOld) {
                    Block.popResource(getLevel(), getBlockPos(), prev.getItem().getItemWhenRemoved(prev));
                }
            }
        }
        clientSyncDataChanged();
        return prev;
    }

    public @Nullable LocalAxonConnection removeUpstream(@NotNull AxonType type, boolean drop) {
        LocalAxonConnection prev = upstream.remove(type);
        if (prev != null && getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(prev.getTargetPos());
            if (be instanceof AxonBlockEntity a) a.removeDownstream(getBlockPos());
            if (drop) {
                Block.popResource(getLevel(), getBlockPos(), prev.getItem().getItemWhenRemoved(prev));
            }
            clientSyncDataChanged();
        }
        return prev;
    }

    public @NotNull @UnmodifiableView Map<AxonType, LocalAxonConnection> getUpstream() {
        return upstream;
    }

    public @NotNull @UnmodifiableView Set<BlockPos> getDownstream() {
        return downstream;
    }

    public boolean removeDownstream(@NotNull BlockPos pos) {
        boolean changed = downstream.remove(pos);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    public boolean addDownstream(@NotNull BlockPos pos) {
        boolean changed = downstream.add(pos);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        UPSTREAM_CODEC.encodeStart(NbtOps.INSTANCE, upstream)
                .get().ifLeft(t -> tag.put("Upstream", t));
        return tag;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        upstream.clear();
        UPSTREAM_CODEC.parse(NbtOps.INSTANCE, tag.get("Upstream"))
                .get().ifLeft(upstream::putAll);
        if (tag.contains("Downstream")) {
            downstream.clear();
            DOWNSTREAM_CODEC.parse(NbtOps.INSTANCE, tag.get("Downstream"))
                    .get().ifLeft(downstream::addAll);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        UPSTREAM_CODEC.encodeStart(NbtOps.INSTANCE, upstream)
                .get().ifLeft(t -> tag.put("Upstream", t));
        DOWNSTREAM_CODEC.encodeStart(NbtOps.INSTANCE, downstream)
                .get().ifLeft(t -> tag.put("Downstream", t));
    }

    @Override
    public void setRemoved() {
        if (!(getLevel() instanceof ServerLevel level) || level.getServer().isCurrentlySaving()) return;
        super.setRemoved();
        for (BlockPos pos : downstream) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AxonBlockEntity abe) {
                abe.upstreamRemoved();
            }
        }
        for (LocalAxonConnection connection : upstream.values()) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof AxonBlockEntity a) {
                a.removeDownstream(getBlockPos());
            }
            Block.popResource(getLevel(), getBlockPos(), connection.getItem().getItemWhenRemoved(connection));
        }
    }

    public void upstreamRemoved() {
        if (getLevel() == null) return;
        for (Iterator<Map.Entry<AxonType, LocalAxonConnection>> iterator = upstream.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            BlockEntity be = getLevel().getBlockEntity(entry.getValue().getTargetPos());
            if (!(be instanceof AxonBlockEntity) || be.isRemoved()) {
                Block.popResource(getLevel(), entry.getValue().getTargetPos(), entry.getValue().getItem().getItemWhenRemoved(entry.getValue()));
                clientSyncDataChanged();
                iterator.remove();
            }
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void clientSyncDataChanged() {
        if (getLevel() != null) {
            setChanged();
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }
}
