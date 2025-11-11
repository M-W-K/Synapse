package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.DeviceDataKeys;
import com.m_w_k.synapse.common.block.RelayBlock;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        AxonDeviceDefinitions.RELAYS.forEach((pair, resloc) -> {
            devices.put(resloc, new LocalConnectorDevice(pair.value(), ConnectorLevel.RELAY));
        });
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
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof IAxonBlockEntity a) {
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
    public boolean allowsUpstream(ResourceLocation slot, LocalConnectorDevice upstream) {
        if (!super.allowsUpstream(slot, upstream)) return false;
        LocalConnectorDevice us = getBySlot(slot);
        if (SynapseUtil.actualLevel(us) != ConnectorLevel.RELAY) {
            // only allow promoting our actual level to prevent broken behavior
            if (SynapseUtil.actualLevel(us).getPrio() > SynapseUtil.actualLevel(upstream).getPrio()) return false;
        }
        return ConnectorLevel.ADDRESS_SPACE.contains(SynapseUtil.actualLevel(upstream));
    }

    @Override
    public boolean allowsDownstream(ResourceLocation slot, LocalConnectorDevice downstream) {
        if (getLevel() == null) return false;
        LocalConnectorDevice device = getBySlot(slot).ensureRegistered(getLevel());
        return super.allowsDownstream(slot, downstream) && device.upstream() != null
                && (device.cache() == null || !device.cache().downstream().hasNext());
    }

    @Override
    public @NotNull String getNameBySlot(ResourceLocation slot) {
        var pair = AxonDeviceDefinitions.relay(slot, true);
        return pair.value().name() + "_" + pair.keyInt();
    }

    @Override
    public boolean slotIsActive(ResourceLocation slot) {
        return super.slotIsActive(slot) && getBlockState().getValue(RelayBlock.RELAYS) > AxonDeviceDefinitions.relay(slot, true).firstInt();
    }
}
