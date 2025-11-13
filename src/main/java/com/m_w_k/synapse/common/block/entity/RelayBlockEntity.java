package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.ModuleDataProtocols;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.DeviceDataKeys;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.common.block.RelayBlock;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Relays are a critical component of any large network that allows extending the effective
 * connection length of a distributor.
 */
public class RelayBlockEntity extends AxonBlockEntity {
    protected static final Vec3[] SOUTH_CENTERS = new Vec3[] {
            new Vec3(-4/16d, 4/16d, -6/16d),
            new Vec3(4/16d, 4/16d, -6/16d),
            new Vec3(4/16d, -4/16d, -6/16d),
            new Vec3(-4/16d, -4/16d, -6/16d)
    };
    protected static final Vec3[] WEST_CENTERS = new Vec3[] {
            new Vec3(6/16d, 4/16d, -4/16d),
            new Vec3(6/16d, 4/16d, 4/16d),
            new Vec3(6/16d, -4/16d, 4/16d),
            new Vec3(6/16d, -4/16d, -4/16d)
    };
    protected static final Vec3[] NORTH_CENTERS = new Vec3[] {
            new Vec3(4/16d, 4/16d, 6/16d),
            new Vec3(-4/16d, 4/16d, 6/16d),
            new Vec3(-4/16d, -4/16d, 6/16d),
            new Vec3(4/16d, -4/16d, 6/16d)
    };
    protected static final Vec3[] EAST_CENTERS = new Vec3[] {
            new Vec3(-6/16d, 4/16d, 4/16d),
            new Vec3(-6/16d, 4/16d, -4/16d),
            new Vec3(-6/16d, -4/16d, -4/16d),
            new Vec3(-6/16d, -4/16d, 4/16d)
    };
    protected static final Vec3[] DOWN_CENTERS = new Vec3[] {
            new Vec3(4/16d, 6/16d, 4/16d),
            new Vec3(4/16d, 6/16d, -4/16d),
            new Vec3(-4/16d, 6/16d, -4/16d),
            new Vec3(-4/16d, 6/16d, 4/16d)
    };
    protected static final Vec3[] UP_CENTERS = new Vec3[] {
            new Vec3(-4/16d, -6/16d, 4/16d),
            new Vec3(-4/16d, -6/16d, -4/16d),
            new Vec3(4/16d, -6/16d, -4/16d),
            new Vec3(4/16d, -6/16d, 4/16d)
    };

    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(SynapseBlockEntityRegistry.RELAY_BLOCK.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() == null) return;
        // check for improper blockstate as a backwards compatibility datafix
        BlockState state = getBlockState();
        for (BooleanProperty prop : RelayBlock.PROPERTY_BY_INT) {
            if (state.getValue(prop)) return;
        }
        for (int i = 0; i < state.getValue(RelayBlock.RELAYS); i++) {
            state = state.setValue(RelayBlock.PROPERTY_BY_INT[i], true);
        }
        getLevel().setBlock(getBlockPos(), state, Block.UPDATE_CLIENTS + Block.UPDATE_KNOWN_SHAPE);
    }

    @Override
    public @NotNull Vec3 renderOffsetForSlot(ResourceLocation slot, IAxonBlockEntity other) {
        Direction dir = getBlockState().getValue(RelayBlock.MOUNT_DIRECTION);

        return centers(dir)[AxonDeviceDefinitions.relay(slot, true).firstInt()];
    }

    public static Vec3[] centers(Direction mount) {
        return switch (mount) {
            case DOWN -> DOWN_CENTERS;
            case UP -> UP_CENTERS;
            case NORTH -> NORTH_CENTERS;
            case SOUTH -> SOUTH_CENTERS;
            case WEST -> WEST_CENTERS;
            case EAST -> EAST_CENTERS;
        };
    }

    @Override
    public @NotNull Vec3 renderDirectionForSlot(ResourceLocation slot, IAxonBlockEntity other) {
        Vec3i norm = getBlockState().getValue(RelayBlock.MOUNT_DIRECTION).getNormal();
        return new Vec3(norm.getX(), norm.getY(), norm.getZ());
    }

    @Override
    public @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld) {
        if (getLevel() != null && hasSlot(connection.getSourceSlot())) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof IAxonBlockEntity a && a.hasSlot(connection.getTargetSlot())) {
                LocalConnectorDevice device = getBySlot(connection.getSourceSlot());
                device.getData().put(DeviceDataKeys.RELAYING, SynapseUtil.actualLevel(a.getBySlot(connection.getTargetSlot())));
                var treeDevice = device.cache();
                if (treeDevice != null && treeDevice.hasUpstream()) {
                    treeDevice.getData().put(DeviceDataKeys.RELAYING, SynapseUtil.actualLevel(treeDevice.getUpstream()));
                }
            }
        }
        return super.setUpstream(connection, dropOld);
    }

    @Override
    public boolean allowsUpstream(@NotNull ResourceLocation slot, LocalConnectorDevice upstream) {
        if (!super.allowsUpstream(slot, upstream)) return false;
        LocalConnectorDevice us = getBySlot(slot);
        if (SynapseUtil.actualLevel(us) != ConnectorLevel.RELAY) {
            // only allow promoting our actual level to prevent broken behavior
            if (SynapseUtil.actualLevel(us).getPrio() > SynapseUtil.actualLevel(upstream).getPrio()) return false;
        }
        return ConnectorLevel.ADDRESS_SPACE.contains(SynapseUtil.actualLevel(upstream));
    }

    @Override
    public boolean allowsDownstream(@NotNull ResourceLocation slot, LocalConnectorDevice downstream) {
        if (getLevel() == null || !hasSlot(slot)) return false;
        LocalConnectorDevice device = getBySlot(slot).ensureRegistered(getLevel());
        return super.allowsDownstream(slot, downstream) && device.upstream() != null
                && (device.cache() == null || !device.cache().downstream().hasNext());
    }

    @Override
    public @NotNull String getNameBySlot(@NotNull ResourceLocation slot) {
        var pair = AxonDeviceDefinitions.relay(slot, true);
        return pair.value().name() + "_" + pair.keyInt();
    }

    @Override
    public void installModules(@NotNull AxonBlockItem item, @NotNull BlockPlaceContext context) {
        ModuleDataProtocols.readRelayModules(this::installModule, context.getItemInHand(),
                RelayBlock.getKeyComponent(context.getHitResult(), getBlockPos(), getBlockState()));
    }

    @Override
    public @NotNull LocalConnectorDevice installModule(@NotNull ResourceLocation slotToAdd) {
        return devices.computeIfAbsent(slotToAdd, k -> new LocalConnectorDevice(AxonDeviceDefinitions.relay((ResourceLocation) k, true).value(), ConnectorLevel.RELAY));
    }

    @Override
    protected Optional<CompoundTag> writeModuleData() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            ModuleDataProtocols.writeRelayModules(devices, i)
                    .ifPresent(t -> tag.put(ModuleDataProtocols.relayBEKey(finalI), t));
        }
        return tag.isEmpty() ? Optional.empty() : Optional.of(tag);
    }
}
