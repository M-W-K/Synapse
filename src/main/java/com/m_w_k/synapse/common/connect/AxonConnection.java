package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Connection data owned by an axon tree
 */
public sealed class AxonConnection permits LocalAxonConnection {

    public static final Codec<AxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(AxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionTier.CONNECTION_TYPES[i], ConnectionTier.Type::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, AxonConnection::new));

    private final @NotNull AxonType axonType;
    private @NotNull CompoundTag data;

    private @NotNull ConnectionTier.Type connectionType;

    public AxonConnection(@NotNull AxonType axonType) {
        this(axonType, null);
    }

    public AxonConnection(@NotNull AxonType axonType, ConnectionTier.@Nullable Type connectionType) {
        this(axonType, new CompoundTag(), connectionType);
    }

    protected AxonConnection(@NotNull AxonType axonType, CompoundTag tag, ConnectionTier.@Nullable Type connectionType) {
        this.axonType = axonType;
        this.data = tag;
        this.connectionType = connectionType == null ? ConnectionTier.Type.UNKNOWN : connectionType;
    }

    public long getCapacity(Level level, long requested, boolean simulate) {
        long tick = level.getGameTime();
        long prev = data.getLong("LastTick");
        long cap = axonType.getProvider().getCapacity(requested, data, (int) (tick - prev), simulate);
        data.putLong("LastTick", tick);
        return cap;
    }

    protected @NotNull CompoundTag getData() {
        return data;
    }

    public @NotNull AxonType getAxonType() {
        return axonType;
    }

    public void updateConnectionType(@NotNull ConnectionTier.Type connectionType) {
        this.connectionType = connectionType;
    }

    public @NotNull ConnectionTier.Type getConnectionType() {
        return connectionType;
    }
}
