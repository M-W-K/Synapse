package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IAxonBlockEntity extends ICapabilityProvider, IForgeBlockEntity {

    boolean removed();

    @Nullable Level level();

    @NotNull BlockPos blockPos();

    int getSlots();

    @NotNull LocalConnectorDevice getBySlot(int slot);

    default boolean slotIsActive(int slot) {
        return true;
    }

    @NotNull String getNameBySlot(int slot);

    default boolean allowsUpstream(int slot, LocalConnectorDevice upstream) {
        return slotIsActive(slot);
    }

    default boolean allowsDownstream(int slot, LocalConnectorDevice downstream) {
        return slotIsActive(slot);
    }

    void notifyChanged();

    @NotNull Vec3 renderOffsetForSlot(int slot, IAxonBlockEntity other);

    @NotNull Vec3 renderDirectionForSlot(int slot, IAxonBlockEntity other);

    @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld);

    boolean removeDownstream(@NotNull BlockPos pos);

    boolean addDownstream(@NotNull BlockPos pos);

    void onUpstreamRemoved();

    boolean removeUpstreamFrom(int slot);

    void removeDownstreamFrom(int slot);

    void retireSlot(int slot);
}
