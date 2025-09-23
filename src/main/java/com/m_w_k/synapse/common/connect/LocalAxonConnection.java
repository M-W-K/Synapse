package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.common.item.AxonItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Connection data owned by a block entity
 */
public final class LocalAxonConnection extends AxonConnection {

    public static final Codec<LocalAxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.ITEMS.getCodec().xmap(i -> (AxonItem) i, i -> i).fieldOf("item").forGetter(LocalAxonConnection::getItem),
                    Codec.INT.fieldOf("sourceSlot").forGetter(LocalAxonConnection::getSourceSlot),
                    BlockPos.CODEC.fieldOf("targetPos").forGetter(LocalAxonConnection::getTargetPos),
                    Codec.INT.fieldOf("targetSlot").forGetter(LocalAxonConnection::getTargetSlot),
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(AxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionTier.CONNECTION_TYPES[i], ConnectionTier.Type::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, LocalAxonConnection::new));

    private final @NotNull AxonItem item;

    private final int sourceSlot;
    private final BlockPos targetPos;
    private final int targetSlot;

    public LocalAxonConnection(@NotNull AxonItem item, int sourceSlot, BlockPos targetPos, int targetSlot, AxonType axonType,
                               ConnectionTier.@Nullable Type connectionType) {
        super(axonType, connectionType);
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
    }

    private LocalAxonConnection(@NotNull AxonItem item, int sourceSlot, BlockPos targetPos, int targetSlot, AxonType axonType,
                                CompoundTag tag, ConnectionTier.@Nullable Type connectionType) {
        super(axonType, tag, connectionType);
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
    }

    public @NotNull AxonItem getItem() {
        return item;
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
