package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.OldAxonDeviceDefinitions;
import com.m_w_k.synapse.api.connect.AxonConnection;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.common.block.entity.RelayBlockEntity;
import com.m_w_k.synapse.common.item.AxonItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OldLocalAxonConnection extends LocalAxonConnection {

    public static final Codec<OldLocalAxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.ITEMS.getCodec().xmap(i -> (AxonItem) i, i -> i).fieldOf("item").forGetter(LocalAxonConnection::getItem),
                    Codec.INT.fieldOf("sourceSlot").forGetter(con -> con.sourceSlot),
                    Vec3.CODEC.fieldOf("sourceRenderOffset").forGetter(LocalAxonConnection::getSourceRenderOffset),
                    Vec3.CODEC.fieldOf("sourceRenderDirection").forGetter(LocalAxonConnection::getSourceRenderDirection),
                    BlockPos.CODEC.fieldOf("targetPos").forGetter(LocalAxonConnection::getTargetPos),
                    Codec.INT.fieldOf("targetSlot").forGetter(con -> con.targetSlot),
                    Vec3.CODEC.fieldOf("targetRenderOffset").forGetter(LocalAxonConnection::getTargetRenderOffset),
                    Vec3.CODEC.fieldOf("targetRenderDirection").forGetter(LocalAxonConnection::getTargetRenderDirection),
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(LocalAxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionType.TYPES[i], ConnectionType::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, OldLocalAxonConnection::new));

    private final int sourceSlot;
    private @Nullable ResourceLocation sourceSlotDetermine;
    private final int targetSlot;
    private @Nullable ResourceLocation targetSlotDetermine;

    public OldLocalAxonConnection(@NotNull AxonItem item, int sourceSlot, Vec3 sourceRenderOffset, Vec3 sourceRenderDirection, BlockPos targetPos, int targetSlot, Vec3 targetRenderOffset, Vec3 targetRenderDirection, AxonType axonType, CompoundTag tag, @Nullable ConnectionType connectionType) {
        super(item, null, sourceRenderOffset, sourceRenderDirection, targetPos, null, targetRenderOffset, targetRenderDirection, axonType, tag, connectionType);
        this.sourceSlot = sourceSlot;
        this.targetSlot = targetSlot;
    }

    @Override
    public ResourceLocation getSourceSlot() {
        return sourceSlotDetermine;
    }

    @Override
    public ResourceLocation getSourceSlotOldDataSafe(@NotNull IAxonBlockEntity be) {
        if (sourceSlotDetermine == null) {
            var m = OldAxonDeviceDefinitions.getModernizer(count(be));
            if (m != null) sourceSlotDetermine = m.apply(sourceSlot);
        }
        return getSourceSlot();
    }

    @Override
    public ResourceLocation getTargetSlot() {
        return targetSlotDetermine;
    }

    @Override
    public ResourceLocation getTargetSlotOldDataSafe(@NotNull IAxonBlockEntity be) {
        if (targetSlotDetermine == null) {
            var m = OldAxonDeviceDefinitions.getModernizer(count(be));
            if (m != null) targetSlotDetermine = m.apply(targetSlot);
        }
        return getTargetSlot();
    }

    private static int count(IAxonBlockEntity be) {
        if (be instanceof EndpointBlockEntity) {
            return OldAxonDeviceDefinitions.ENDPOINTS_INV.size();
        } else if (be instanceof RelayBlockEntity) {
            return OldAxonDeviceDefinitions.RELAYS_INV.size();
        } else {
            return OldAxonDeviceDefinitions.STANDARD_INV.size();
        }
    }

}
