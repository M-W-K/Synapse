package com.m_w_k.synapse.api.connect;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.client.renderer.AxonTexDescription;
import com.m_w_k.synapse.config.SynapseConfigs;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AxonType implements StringRepresentable, IExtensibleEnum {
    ENERGY((r, d, t, sim) -> {
        long capacity = d.getLong("Capacity");
        long consumed = d.getLong("Consumed");
        if (t > 0) {
            capacity += (int) (consumed * energyResponse());
            consumed = 0;
            capacity = (int) (capacity * Math.pow(1 - energyDecay(), t));
            if (!sim) d.putLong("Capacity", capacity);
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        capacity += r / 10;
        return capacity - consumed;
    }, ForgeCapabilities.ENERGY, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 3, 6, 0, 16)),
    ITEM((r, d, t, sim) -> {
        int baseCap = baseItemCap();
        long capacity = d.getInt("Capacity");
        long consumed = d.getInt("Consumed");
        if (t > 0) {
            int refreshInterval = 10;
            t = t + d.getInt("TimeSum");
            if (!sim) d.putInt("TimeSum", t % refreshInterval);
            t = t / refreshInterval;
            if (t > 0) {
                capacity += Math.min(baseCap, consumed);
                if (!sim) d.remove("Consumed");
                consumed = 0;
                if (t > 1) {
                    capacity = Math.max(baseCap, capacity - baseCap * (t - 1L));
                }
                if (!sim) d.putLong("Capacity", capacity);
            }
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        return capacity - consumed;
    }, ForgeCapabilities.ITEM_HANDLER, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 6, 9, 0, 16)),
    FLUID((r, d, t, sim) -> {
        long baseCap = baseFluidCap();
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
    }, ForgeCapabilities.FLUID_HANDLER, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 0, 3, 0, 16)),
    REDSTONE(null, null, new AxonTexDescription(SynapseUtil.resLoc("block/axon_texture"), 0, 3, 0, 16));

    public static final Codec<AxonType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(AxonType::values, AxonType::valueOf);

    private final @NotNull ResourceLocation resloc;
    private final @Nullable CapacityProvider provider;
    private final @Nullable Capability<?> capability;
    private final @NotNull AxonTexDescription tex;

    AxonType(@Nullable CapacityProvider provider, @Nullable Capability<?> capability, @NotNull AxonTexDescription tex) {
        this.resloc = SynapseUtil.resLoc(name().toLowerCase());
        this.provider = provider;
        this.capability = capability;
        this.tex = tex;
    }

    AxonType(@NotNull ResourceLocation resloc, @Nullable CapacityProvider provider, @Nullable Capability<?> capability, @NotNull AxonTexDescription tex) {
        this.resloc = resloc;
        this.provider = provider;
        this.capability = capability;
        this.tex = tex;
    }

    public static AxonType add(ResourceLocation resloc, CapacityProvider provider, @NotNull Capability<?> capability, @NotNull AxonTexDescription tex) {
        return create(resloc.toString().toUpperCase(), resloc, provider, capability, tex);
    }

    public static AxonType create(String name, ResourceLocation resloc, CapacityProvider provider, @NotNull Capability<?> capability, @NotNull AxonTexDescription tex) {
        throw new IllegalStateException("Enum not extended");
    }

    public @NotNull ResourceLocation getResloc() {
        return resloc;
    }

    public @Nullable CapacityProvider getProvider() {
        return provider;
    }

    public @Nullable Capability<?> getCapability() {
        return capability;
    }

    public @NotNull AxonTexDescription getTex() {
        return tex;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }

    private static int refreshInterval() {
        return SynapseConfigs.server().network.capacityRefreshInterval.get();
    }

    private static int baseItemCap() {
        return SynapseConfigs.server().network.baseItemCapacity.get();
    }

    private static int baseFluidCap() {
        return SynapseConfigs.server().network.baseFluidCapacity.get();
    }

    private static float energyResponse() {
        return SynapseConfigs.server().network.energyCapacityResponseFactor.getF();
    }

    private static float energyDecay() {
        return SynapseConfigs.server().network.energyCapacityDecayFactor.getF();
    }
}
