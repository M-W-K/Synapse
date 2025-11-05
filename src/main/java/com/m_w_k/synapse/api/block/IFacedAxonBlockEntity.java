package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IFacedAxonBlockEntity extends IAxonBlockEntity {

    @NotNull ResourceLocation getSlotForFace(@NotNull Direction face, @NotNull AxonType type);

    default @NotNull LocalConnectorDevice getByFace(@NotNull Direction face, @NotNull AxonType type) {
        return getBySlot(getSlotForFace(face, type));
    }

    default @NotNull String getNameByFace(@NotNull Direction face, @NotNull AxonType type) {
        return getNameBySlot(getSlotForFace(face, type));
    }
}
