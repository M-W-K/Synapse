package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.function.BiConsumer;

public interface IAxonBlockEntity extends ICapabilityProvider, IForgeBlockEntity {

    boolean removed();

    @Nullable Level level();

    @NotNull BlockPos blockPos();

    @NotNull @UnmodifiableView Map<ResourceLocation, LocalConnectorDevice> getSlots();

    @NotNull LocalConnectorDevice getBySlot(ResourceLocation slot);

    boolean hasSlot(@Nullable ResourceLocation slot);

    default boolean slotIsActive(ResourceLocation slot) {
        return true;
    }

    @NotNull String getNameBySlot(ResourceLocation slot);

    default boolean allowsUpstream(ResourceLocation slot, LocalConnectorDevice upstream) {
        return slotIsActive(slot);
    }

    default boolean allowsDownstream(ResourceLocation slot, LocalConnectorDevice downstream) {
        return slotIsActive(slot);
    }

    void notifyChanged();

    @NotNull Vec3 renderOffsetForSlot(ResourceLocation slot, IAxonBlockEntity other);

    @NotNull Vec3 renderDirectionForSlot(ResourceLocation slot, IAxonBlockEntity other);

    @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld);

    boolean removeDownstream(@NotNull BlockPos pos);

    boolean addDownstream(@NotNull BlockPos pos);

    void onUpstreamRemoved();

    boolean removeUpstreamFrom(ResourceLocation slot);

    void removeDownstreamFrom(ResourceLocation slot);

    void retireSlot(ResourceLocation slot);
}
