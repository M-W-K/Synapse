package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonConnection;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
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

/**
 * Connection data owned by a block entity
 */
public sealed class LocalAxonConnection extends AxonConnection permits OldLocalAxonConnection  {

    public static final Codec<LocalAxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.ITEMS.getCodec().xmap(i -> (AxonItem) i, i -> i).fieldOf("item").forGetter(LocalAxonConnection::getItem),
                    ResourceLocation.CODEC.fieldOf("sourceSlot").forGetter(LocalAxonConnection::getSourceSlot),
                    Vec3.CODEC.fieldOf("sourceRenderOffset").forGetter(LocalAxonConnection::getSourceRenderOffset),
                    Vec3.CODEC.fieldOf("sourceRenderDirection").forGetter(LocalAxonConnection::getSourceRenderDirection),
                    BlockPos.CODEC.fieldOf("targetPos").forGetter(LocalAxonConnection::getTargetPos),
                    ResourceLocation.CODEC.fieldOf("targetSlot").forGetter(LocalAxonConnection::getTargetSlot),
                    Vec3.CODEC.fieldOf("targetRenderOffset").forGetter(LocalAxonConnection::getTargetRenderOffset),
                    Vec3.CODEC.fieldOf("targetRenderDirection").forGetter(LocalAxonConnection::getTargetRenderDirection),
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(LocalAxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionType.TYPES[i], ConnectionType::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, LocalAxonConnection::new));

    private final @NotNull AxonItem item;

    private final ResourceLocation sourceSlot;
    private final Vec3 sourceRenderOffset;
    private final Vec3 sourceRenderDirection;
    private final BlockPos targetPos;
    private final ResourceLocation targetSlot;
    private final Vec3 targetRenderOffset;
    private final Vec3 targetRenderDirection;

    public LocalAxonConnection(@NotNull AxonItem item, ResourceLocation sourceSlot, Vec3 sourceRenderOffset, Vec3 sourceRenderDirection,
                               BlockPos targetPos, ResourceLocation targetSlot, Vec3 targetRenderOffset, Vec3 targetRenderDirection,
                               AxonType axonType, @Nullable ConnectionType connectionType) {
        super(axonType, connectionType);
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.sourceRenderOffset = sourceRenderOffset;
        this.sourceRenderDirection = sourceRenderDirection;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
        this.targetRenderOffset = targetRenderOffset;
        this.targetRenderDirection = targetRenderDirection;
    }

    protected LocalAxonConnection(@NotNull AxonItem item, ResourceLocation sourceSlot, Vec3 sourceRenderOffset, Vec3 sourceRenderDirection,
                                BlockPos targetPos, ResourceLocation targetSlot, Vec3 targetRenderOffset, Vec3 targetRenderDirection,
                                AxonType axonType, CompoundTag tag, @Nullable ConnectionType connectionType) {
        super(axonType, tag, connectionType);
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.sourceRenderOffset = sourceRenderOffset;
        this.sourceRenderDirection = sourceRenderDirection;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
        this.targetRenderOffset = targetRenderOffset;
        this.targetRenderDirection = targetRenderDirection;
    }

    @Override
    protected @NotNull CompoundTag getData() {
        return super.getData();
    }

    public @NotNull AxonItem getItem() {
        return item;
    }

    public ResourceLocation getSourceSlot() {
        return sourceSlot;
    }

    public ResourceLocation getSourceSlotOldDataSafe(@NotNull IAxonBlockEntity be) {
        return sourceSlot;
    }

    public Vec3 getSourceRenderOffset() {
        return sourceRenderOffset;
    }

    public Vec3 getSourceRenderDirection() {
        return sourceRenderDirection;
    }

    public ResourceLocation getTargetSlot() {
        return targetSlot;
    }

    public ResourceLocation getTargetSlotOldDataSafe(@NotNull IAxonBlockEntity be) {
        return targetSlot;
    }

    public Vec3 getTargetRenderOffset() {
        return targetRenderOffset;
    }

    public Vec3 getTargetRenderDirection() {
        return targetRenderDirection;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }
}
