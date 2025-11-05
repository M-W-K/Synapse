package com.m_w_k.synapse.network;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.BitSet;
import java.util.Set;

public class ClientboundDeviceSyncPacket {

    protected final Set<ResourceLocation> activeDevices;

    public ClientboundDeviceSyncPacket(Set<ResourceLocation> activeDevices) {
        this.activeDevices = activeDevices;
    }
    public ClientboundDeviceSyncPacket(FriendlyByteBuf buf) {
        activeDevices = new ObjectOpenHashSet<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            activeDevices.add(buf.readResourceLocation());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(activeDevices.size());
        for (ResourceLocation resloc : activeDevices) {
            buf.writeResourceLocation(resloc);
        }
    }

    public Set<ResourceLocation> getActiveDevices() {
        return activeDevices;
    }
}
