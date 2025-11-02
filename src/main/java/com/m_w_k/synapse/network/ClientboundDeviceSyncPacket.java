package com.m_w_k.synapse.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.BitSet;

public class ClientboundDeviceSyncPacket {

    protected final BitSet activeDevices;

    public ClientboundDeviceSyncPacket(BitSet activeDevices) {
        this.activeDevices = activeDevices;
    }
    public ClientboundDeviceSyncPacket(FriendlyByteBuf buf) {
        activeDevices = buf.readBitSet();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBitSet(activeDevices);
    }

    public BitSet getActiveDevices() {
        return activeDevices;
    }
}
