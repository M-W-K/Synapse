package com.m_w_k.synapse.api.connect;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.client.gui.TexDefinition;
import com.m_w_k.synapse.client.renderer.AxonTexDescription;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public enum AxonType implements StringRepresentable, IExtensibleEnum {
    ENERGY((r, d, t, sim) -> {
        long capacity = d.getLong("Capacity");
        long consumed = d.getLong("Consumed");
        if (t > 0) {
            capacity += (int) (consumed * 0.1f);
            consumed = 0;
            capacity = (int) (capacity * Math.pow(0.95f, t));
            if (!sim) d.putLong("Capacity", capacity);
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        capacity += r / 10;
        return capacity - consumed;
    }, ForgeCapabilities.ENERGY, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 3, 6, 0, 16)),
    ITEM((r, d, t, sim) -> {
        int baseStackCap = 1;
        long capacity = d.getInt("Capacity");
        long consumed = d.getInt("Consumed");
        if (t > 0) {
            int refreshInterval = 10;
            t = t + d.getInt("TimeSum");
            if (!sim) d.putInt("TimeSum", t % refreshInterval);
            t = t / refreshInterval;
            if (t > 0) {
                capacity += Math.min(baseStackCap * 64, consumed);
                if (!sim) d.remove("Consumed");
                consumed = 0;
                if (t > 1) {
                    capacity = Math.max(baseStackCap * 64, capacity - baseStackCap * 64L * (t - 1));
                }
                if (!sim) d.putLong("Capacity", capacity);
            }
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        return capacity - consumed;
    }, ForgeCapabilities.ITEM_HANDLER, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 6, 9, 0, 16)),
    FLUID((r, d, t, sim) -> {
        long baseCap = 1000;
        long capacity = d.getInt("Capacity");
        int consumed = d.getInt("Consumed");
        if (t > 0) {
            int refreshInterval = 10;
            t = t + d.getInt("TimeSum");
            if (!sim) d.putInt("TimeSum", t % refreshInterval);
            t = t / refreshInterval;
            if (t > 0) {
                capacity += Math.min(consumed, baseCap);
                if (!sim) d.remove("Consumed");
                consumed = 0;
                if (t > 1) {
                    capacity = Math.max(baseCap, capacity - baseCap * (t - 1));
                }
                if (!sim) d.putLong("Capacity", capacity);
            }
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        return capacity - consumed;
    }, ForgeCapabilities.FLUID_HANDLER, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 0, 3, 0, 16));

    public static final Codec<AxonType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(AxonType::values, AxonType::valueOf);

    private final @NotNull CapacityProvider provider;
    private final @NotNull Capability<?> capability;
    private final @NotNull AxonTexDescription tex;

    AxonType(@NotNull CapacityProvider provider, @NotNull Capability<?> capability, @NotNull AxonTexDescription tex) {
        this.provider = provider;
        this.capability = capability;
        this.tex = tex;
    }

    public static AxonType create(String name, CapacityProvider provider, @NotNull Capability<?> capability, @NotNull AxonTexDescription tex) {
        throw new IllegalStateException("Enum not extended");
    }

    public @NotNull CapacityProvider getProvider() {
        return provider;
    }

    public @NotNull Capability<?> getCapability() {
        return capability;
    }

    public @NotNull AxonTexDescription getTex() {
        return tex;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
