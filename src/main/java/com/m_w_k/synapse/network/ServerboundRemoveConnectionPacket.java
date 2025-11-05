package com.m_w_k.synapse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ServerboundRemoveConnectionPacket {

    protected final ResourceLocation slot;

    public ServerboundRemoveConnectionPacket(ResourceLocation slot) {
        this.slot = slot;
    }

    public ServerboundRemoveConnectionPacket(FriendlyByteBuf buf) {
        this.slot = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(slot);
    }

    public ResourceLocation getSlot() {
        return slot;
    }
}
