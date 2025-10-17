package com.m_w_k.synapse.client.gui;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record TexLocation(@NotNull ResourceLocation loc, int xSize, int ySize) {
    public static TexLocation standard(@NotNull ResourceLocation loc) {
        return new TexLocation(loc, 256, 256);
    }
}
