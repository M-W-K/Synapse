package com.m_w_k.synapse.network;

import net.minecraft.network.FriendlyByteBuf;

public class ServerboundRemoveConnectionPacket {

    protected final int slot;

    public ServerboundRemoveConnectionPacket(int slot) {
        this.slot = slot;
    }

    public ServerboundRemoveConnectionPacket(FriendlyByteBuf buf) {
        this.slot = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    public int getSlot() {
        return slot;
    }
}
