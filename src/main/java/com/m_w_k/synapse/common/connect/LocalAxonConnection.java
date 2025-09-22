package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Connection data owned by a block entity
 */
public final class LocalAxonConnection extends AxonConnection {

    public static final Codec<LocalAxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("sourceSlot").forGetter(LocalAxonConnection::getSourceSlot),
                    BlockPos.CODEC.fieldOf("targetPos").forGetter(LocalAxonConnection::getTargetPos),
                    Codec.INT.fieldOf("targetSlot").forGetter(LocalAxonConnection::getTargetSlot),
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(AxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionTier.CONNECTION_TYPES[i], ConnectionTier.Type::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, LocalAxonConnection::new));

    private final int sourceSlot;
    private final BlockPos targetPos;
    private final int targetSlot;

    public LocalAxonConnection(int sourceSlot, BlockPos targetPos, int targetSlot, AxonType axonType,
                               ConnectionTier.@Nullable Type connectionType) {
        super(axonType, connectionType);
        this.sourceSlot = sourceSlot;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
    }

    private LocalAxonConnection(int sourceSlot, BlockPos targetPos, int targetSlot, AxonType axonType,
                               CompoundTag tag, ConnectionTier.@Nullable Type connectionType) {
        super(axonType, tag, connectionType);
        this.sourceSlot = sourceSlot;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
    }

    public int getSourceSlot() {
        return sourceSlot;
    }

    public int getTargetSlot() {
        return targetSlot;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }
}
