package com.m_w_k.synapse.client.renderer;

import net.minecraft.resources.ResourceLocation;

public record AxonTexDescription(ResourceLocation loc, double uMin, double uMax, double vMin, double vMax, double lengthToVFactor) {

    public AxonTexDescription(ResourceLocation loc, double uMin, double uMax, double vMin, double vMax) {
        this(loc, uMin, uMax, vMin, vMax, 16);
    }
}
