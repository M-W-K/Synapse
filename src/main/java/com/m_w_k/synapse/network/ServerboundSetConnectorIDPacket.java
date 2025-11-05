package com.m_w_k.synapse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSetConnectorIDPacket extends ServerboundSetSelectedConnectorPacket {

    protected final short id;

    public ServerboundSetConnectorIDPacket(ResourceLocation slot, short id) {
        super(slot);
        this.id = id;
    }

    public ServerboundSetConnectorIDPacket(FriendlyByteBuf buf) {
        super(buf);
        this.id = buf.readShort();
    }

    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeShort(id);
    }

    public short getId() {
        return id;
    }
}
