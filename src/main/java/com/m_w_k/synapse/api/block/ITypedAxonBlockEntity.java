package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ITypedAxonBlockEntity extends IAxonBlockEntity {

    @NotNull ResourceLocation getSlotForType(@NotNull AxonType type);

    default @Nullable LocalConnectorDevice getByType(@NotNull AxonType type) {
        return getBySlot(getSlotForType(type));
    }

    default boolean hasByType(@NotNull AxonType type) {
        return hasSlot(getSlotForType(type));
    }

    default @NotNull Optional<LocalConnectorDevice> ifByType(@NotNull AxonType type) {
        return ifBySlot(getSlotForType(type));
    }

    default @NotNull String getNameByType(@NotNull AxonType type) {
        return getNameBySlot(getSlotForType(type));
    }
}
