package com.m_w_k.synapse.config;

public class SynapseConfigNetwork extends ConfigBase {

    public final ConfigFloat axonRangeLimit = f(8, 2, 64, "axonRangeLimit", "The maximum distance that Axons can connect blocks across.");

    public final ConfigInt baseItemCapacity = i(64, 64, "baseItemCapacity", "The base number of items that Axons can transfer per interval.");
    public final ConfigInt baseFluidCapacity = i(1000, 1000, "baseFluidCapacity", "The base number of millibuckets that Axons can transfer per interval.");
    public final ConfigInt capacityRefreshInterval = i(10, 1, "capacityRefreshInterval", "The number of ticks between capacity refresh operations.", "Affects how fast capacity grows and shrinks, as well as just base capacity.");
    public final ConfigFloat energyCapacityResponseFactor = f(1f/8, 1f/16, 8f, "energyCapacityResponseFactor", "The factor affecting how fast energy capacity of Axons will grow over time.", "Unaffected by refresh interval.");
    public final ConfigFloat energyCapacityDecayFactor = f(1f/32, 1f/1024, 1f/2, "energyCapacityDecayFactor", "The factor affecting how fast energy capacity of Axons will shrink over time.", "Unaffected by refresh interval.");


    public float getSquareRangeLimit() {
        return axonRangeLimit.getF() * axonRangeLimit.getF();
    }

    @Override
    public String getName() {
        return "network";
    }
}
