package com.m_w_k.synapse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ServerboundSetSelectedConnectorPacket {

    protected final @Nullable ResourceLocation slot;

    public ServerboundSetSelectedConnectorPacket(@Nullable ResourceLocation slot) {
        this.slot = slot;
    }

    public ServerboundSetSelectedConnectorPacket(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            this.slot = buf.readResourceLocation();
        } else {
            this.slot = null;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(slot != null);
        if (slot != null) {
            buf.writeResourceLocation(slot);
        }
    }

    public @Nullable ResourceLocation getSlot() {
        return slot;
    }
}
