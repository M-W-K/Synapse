package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.OldAxonDeviceDefinitions;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.DeviceDataKeys;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public abstract class AxonBlockEntity extends BlockEntity implements IAxonBlockEntity {

    private static final Codec<Collection<BlockPos>> OLD_DOWNSTREAM_CODEC = Codec.list(BlockPos.CODEC).xmap(UnaryOperator.identity(), ObjectArrayList::new);

    protected static final Codec<Object2ReferenceOpenHashMap<ResourceLocation, LocalConnectorDevice>> DEVICE_CODEC = Codec.either(
            Codec.unboundedMap(ResourceLocation.CODEC, LocalConnectorDevice.CODEC).xmap(Object2ReferenceOpenHashMap::new, UnaryOperator.identity()),
            Codec.list(LocalConnectorDevice.CODEC)
    ).xmap(either -> either.map(UnaryOperator.identity(), list -> {
        Object2ReferenceOpenHashMap<ResourceLocation, LocalConnectorDevice> map = new Object2ReferenceOpenHashMap<>(list.size());
        IntFunction<ResourceLocation> modernize = OldAxonDeviceDefinitions.getModernizer(list.size());
        if (modernize == null) return map;
        for (int i = 0; i < list.size(); i++) {
            LocalConnectorDevice device = list.get(i);
            map.put(modernize.apply(i), device);
        }
        return map;
    }), Either::left);
    protected final @NotNull Object2ReferenceOpenHashMap<ResourceLocation, LocalConnectorDevice> devices = new Object2ReferenceOpenHashMap<>();

    public AxonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @NotNull @UnmodifiableView Map<ResourceLocation, LocalConnectorDevice> getSlots() {
        return devices;
    }

    @Override
    public @NotNull LocalConnectorDevice getBySlot(ResourceLocation slot) {
        LocalConnectorDevice ret = devices.get(slot);
        if (ret == null) throw new IllegalArgumentException("Attempted to get a device under slot " + slot + " that did not exist!");
        return ret;
    }

    @Override
    public @NotNull Vec3 renderOffsetForSlot(ResourceLocation slot, IAxonBlockEntity other) {
        return Vec3.ZERO;
    }

    @Override
    public @NotNull Vec3 renderDirectionForSlot(ResourceLocation slot, IAxonBlockEntity other) {
        BlockPos offset = other.blockPos().subtract(this.blockPos());
        Direction.Axis axis = null;
        int val = 0;
        for (Direction.Axis a : Direction.Axis.values()) {
            int o = offset.get(a);
            if (a == Direction.Axis.Y) {
                if (o < 0) o = -o * o - 1;
                else o = (int) Math.sqrt(o - 1);
            }
            if (Math.abs(o) > Math.abs(val)) {
                axis = a;
                val = o;
            }
        }
        if (axis == null) return Vec3.ZERO;
        val = Integer.signum(val);
        return new Vec3(axis == Direction.Axis.X ? val : 0, axis == Direction.Axis.Y ? val : 0, axis == Direction.Axis.Z ? val : 0);
    }

    @Override
    public @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld) {
        LocalAxonConnection prev = getBySlot(connection.getSourceSlot()).setUpstream(connection);
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof IAxonBlockEntity a) a.addDownstream(blockPos(), connection);
            if (prev != null) {
                be = getLevel().getBlockEntity(prev.getTargetPos());
                if (be instanceof IAxonBlockEntity a) a.removeDownstream(blockPos(), connection);
                if (dropOld) {
                    Block.popResource(getLevel(), blockPos(), prev.getItem().getItemWhenRemoved(prev));
                }
            }
        }
        clientSyncDataChanged();
        return prev;
    }

    @Override
    public boolean removeDownstream(@NotNull BlockPos source, @NotNull LocalAxonConnection connection) {
        var downstream = getDownstream(connection.getTargetSlot());
        boolean changed = downstream.remove(Pair.of(source, connection.getSourceSlot()));
        if (changed) {
            notifyChanged();
        }
        return changed;
    }

    @Override
    public boolean addDownstream(@NotNull BlockPos source, @NotNull LocalAxonConnection connection) {
        var downstream = getDownstream(connection.getTargetSlot());
        boolean changed = downstream.add(Pair.of(source, connection.getSourceSlot()));
        if (changed) {
            notifyChanged();
        }
        return changed;
    }

    protected Set<Pair<BlockPos, ResourceLocation>> getDownstream(@NotNull ResourceLocation slot) {
        return DeviceDataKeys.DOWNSTREAM.computeIfAbsent(getBySlot(slot).getData(), ObjectOpenHashSet::new);
    }

    @Override
    public boolean removeUpstreamFrom(ResourceLocation slot) {
        if (getLevel() == null) return false;
        LocalConnectorDevice device = getBySlot(slot);
        LocalAxonConnection connection = device.upstream();
        if (connection == null) return false;
        BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
        Block.popResource(getLevel(), getBlockPos(), connection.getItem().getItemWhenRemoved(connection));
        clientSyncDataChanged();
        device.setUpstream(null);
        if ((be instanceof IAxonBlockEntity a) && !be.isRemoved()) {
            AxonTree.load(getLevel(), connection.getAxonType(), connection.getAxonType().getCapability())
                    .ifPresent(tree -> tree.removeConnection(device.treeID(), null, a.getBySlot(connection.getTargetSlotOldDataSafe(a)).treeID(), null));
            a.removeDownstream(this.getBlockPos(), connection);
        }
        return true;
    }

    @Override
    public void removeDownstreamFrom(ResourceLocation slot) {
        if (getLevel() == null) return;
        LocalConnectorDevice device = getBySlot(slot);
        handleRemovingDownstreamUpstream(slot, device);
        AxonTree.load(getLevel(), device.type(), device.type().getCapability())
                .ifPresent(tree -> {
                    AxonTree<?>.ConnectorDevice treeDevice = tree.get(device.treeID());
                    if (treeDevice == null) return;
                    treeDevice.forEachDownstream(downstream -> {
                        tree.removeConnection(device.treeID(), null, downstream, null);
                    });
                });
    }

    @Override
    public void retireSlot(ResourceLocation slot) {
        if (!(getLevel() instanceof ServerLevel)) return;
        var d1 = devices.get(slot);
        handleRemovingDownstreamUpstream(slot, d1);
        AxonTree.load(getLevel(), d1.type(), d1.type().getCapability())
                .ifPresent(t -> t.retire(d1.treeID()));
        LocalAxonConnection connection = d1.setUpstream(null);
        if (connection == null) return;
        BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
        if (be instanceof IAxonBlockEntity a) {
            a.removeDownstream(getBlockPos(), connection);
        }
        Block.popResource(getLevel(), getBlockPos(), connection.getItem().getItemWhenRemoved(connection));
    }

    protected void handleRemovingDownstreamUpstream(ResourceLocation slot, LocalConnectorDevice device) {
        assert getLevel() != null;
        for (Pair<BlockPos, ResourceLocation> pair : device.getData(DeviceDataKeys.DOWNSTREAM, Collections.emptySet())) {
            BlockEntity be = getLevel().getBlockEntity(pair.getFirst());
            if (be instanceof IAxonBlockEntity abe) {
                if (pair.getSecond() == SynapseUtil.UNKNOWN) {
                    if (abe instanceof AxonBlockEntity b) {
                        b.handleOldUpstreamRemoved(slot, device, getBlockPos());
                    }
                } else {
                    abe.removeUpstreamFrom(pair.getSecond());
                }
            }
        }
    }

    @Override
    public boolean hasSlot(@Nullable ResourceLocation slot) {
        return devices.containsKey(slot);
    }

    @Override
    public boolean slotIsActive(ResourceLocation slot) {
        return hasSlot(slot);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        DEVICE_CODEC.encodeStart(NbtOps.INSTANCE, devices)
                .get().ifLeft(t -> tag.put("Devices", t));
        return tag;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        DEVICE_CODEC.parse(NbtOps.INSTANCE, tag.get("Devices")).get().ifLeft(devices::putAll);
        if (tag.contains("Downstream")) {
            OLD_DOWNSTREAM_CODEC.parse(NbtOps.INSTANCE, tag.get("Downstream")).get()
                    .ifLeft(downstreams -> {
                        // assume that every device has a downstream at every position.
                        // checking for a downstream where we don't have a downstream is fine,
                        // not checking where we do leads to unintended behavior.
                        for (ResourceLocation loc : devices.keySet()) {
                            var downstream = getDownstream(loc);
                            for (BlockPos pos : downstreams) {
                                downstream.add(Pair.of(pos, SynapseUtil.UNKNOWN));
                            }
                        }
                    });
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        DEVICE_CODEC.encodeStart(NbtOps.INSTANCE, devices)
                .get().ifLeft(t -> tag.put("Devices", t));
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        // setRemoved() will be called immediately after this, prevent side effects by clearing our references
        devices.clear();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!(getLevel() instanceof ServerLevel)) return;
        devices.forEach((r, d1) -> {
            handleRemovingDownstreamUpstream(r, d1);
            AxonTree.load(getLevel(), d1.type(), d1.type().getCapability())
                    .ifPresent(t -> t.retire(d1.treeID()));
            LocalAxonConnection connection = d1.upstream();
            if (connection == null) return;
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof IAxonBlockEntity a) {
                a.removeDownstream(blockPos(), connection);
            }
            Block.popResource(getLevel(), blockPos(), connection.getItem().getItemWhenRemoved(connection));
        });
    }

    public void handleOldUpstreamRemoved(ResourceLocation slot, LocalConnectorDevice otherDevice, BlockPos caller) {
        if (getLevel() == null) return;
        devices.values().forEach(d -> {
            LocalAxonConnection connection = d.upstream();
            if (connection == null || !connection.getTargetPos().equals(caller)) return;
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (!(be instanceof IAxonBlockEntity abe) || abe.removed() || slot.equals(connection.getTargetSlotOldDataSafe(abe))) {
                Block.popResource(getLevel(), connection.getTargetPos(), connection.getItem().getItemWhenRemoved(connection));
                clientSyncDataChanged();
                d.setUpstream(null);
            }
        });
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void clientSyncDataChanged() {
        if (getLevel() != null) {
            notifyChanged();
            getLevel().sendBlockUpdated(blockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BoundingBox box = new BoundingBox(blockPos());
        devices.values().forEach(device -> {
            LocalAxonConnection connection = device.upstream();
            if (connection == null) return;
            box.encapsulate(device.upstream().getTargetPos());
        });
        return AABB.of(box).inflate(1);
    }

    // these redirects are required since the interface isn't obfuscated, but the BlockEntity class is.
    @Override
    public @Nullable Level level() {
        return getLevel();
    }

    @Override
    public boolean removed() {
        return isRemoved();
    }

    @Override
    public @NotNull BlockPos blockPos() {
        return getBlockPos();
    }

    @Override
    public void notifyChanged() {
        setChanged();
    }
}
