package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.common.item.ModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface IAxonBlockEntity extends ICapabilityProvider, IForgeBlockEntity {

    boolean removed();

    @Nullable Level level();

    @NotNull BlockPos blockPos();

    @NotNull @UnmodifiableView Map<ResourceLocation, LocalConnectorDevice> getSlots();

    // should throw a descriptive error if hasSlot returns false
    @NotNull LocalConnectorDevice getBySlot(ResourceLocation slot);

    boolean hasSlot(@Nullable ResourceLocation slot);

    default @NotNull Optional<LocalConnectorDevice> ifBySlot(ResourceLocation slot) {
        if (!hasSlot(slot)) return Optional.empty();
        return Optional.of(getBySlot(slot));
    }

    @MustBeInvokedByOverriders
    default boolean slotIsActive(@NotNull ResourceLocation slot) {
        return hasSlot(slot);
    }

    @NotNull String getNameBySlot(@NotNull ResourceLocation slot);

    @MustBeInvokedByOverriders
    default boolean allowsUpstream(@NotNull ResourceLocation slot, LocalConnectorDevice upstream) {
        return slotIsActive(slot);
    }

    @MustBeInvokedByOverriders
    default boolean allowsDownstream(@NotNull ResourceLocation slot, LocalConnectorDevice downstream) {
        return slotIsActive(slot);
    }

    void notifyChanged();

    @NotNull Vec3 renderOffsetForSlot(ResourceLocation slot, IAxonBlockEntity other);

    @NotNull Vec3 renderDirectionForSlot(ResourceLocation slot, IAxonBlockEntity other);

    @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld);

    boolean removeDownstream(@NotNull BlockPos source, @NotNull LocalAxonConnection connection);

    boolean addDownstream(@NotNull BlockPos source, @NotNull LocalAxonConnection connection);

    boolean removeUpstreamFrom(ResourceLocation slot);

    void removeDownstreamFrom(ResourceLocation slot);

    void retireSlot(ResourceLocation slot);

    void installModules(@NotNull AxonBlockItem item, @NotNull BlockPlaceContext context);

    @NotNull LocalConnectorDevice installModule(@NotNull ResourceLocation slotToAdd);
}
