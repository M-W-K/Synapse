package com.m_w_k.synapse.api.connect;

import com.m_w_k.synapse.SynapseUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class DeviceDataKeys {
    public static final DeviceDataKey<ConnectorLevel> RELAYING =
            new CodecDeviceDataKey<>(SynapseUtil.resLoc("relay"), ConnectorLevel.CODEC, r -> ConnectorLevel.CORRUPTED);
    public static final DeviceDataKey<Set<Pair<BlockPos, ResourceLocation>>> DOWNSTREAM =
            new CodecDeviceDataKey<>(SynapseUtil.resLoc("downstream"),
                    SynapseUtil.setCodec(Codec.pair(BlockPos.CODEC.fieldOf("pos").codec(), ResourceLocation.CODEC.fieldOf("loc").codec())),
                    r -> new ObjectOpenHashSet<>());
    public static final DeviceDataKey<ItemStack> MODULE_STACK =
            new CodecDeviceDataKey<>(SynapseUtil.resLoc("module_stack"), ItemStack.CODEC, r -> ItemStack.EMPTY);
}
