package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.ModuleDataProtocols;
import com.m_w_k.synapse.api.block.OldAxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.ITypedAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class TypedAxonBlockEntity extends AxonBlockEntity implements ITypedAxonBlockEntity {

    protected final @NotNull ConnectorLevel level;

    public TypedAxonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, @NotNull ConnectorLevel level) {
        super(type, pos, state);
        this.level = level;
    }

    @Override
    public @NotNull String getNameBySlot(@NotNull ResourceLocation slot) {
        return AxonDeviceDefinitions.standard(slot, true).name();
    }

    @Override
    public @NotNull ResourceLocation getSlotForType(@NotNull AxonType type) {
        return AxonDeviceDefinitions.standard(type, true);
    }

    @Override
    public void installModules(@NotNull AxonBlockItem item, @NotNull BlockPlaceContext context) {
        ModuleDataProtocols.readStandardModules(this::installModule, context.getItemInHand());
    }

    @Override
    public @NotNull LocalConnectorDevice installModule(@NotNull ResourceLocation slotToAdd) {
        return devices.computeIfAbsent(slotToAdd, k -> new LocalConnectorDevice(AxonDeviceDefinitions.standard((ResourceLocation) k, true), level));
    }

    @Override
    protected Optional<CompoundTag> writeModuleData() {
        return ModuleDataProtocols.writeStandardModules(devices);
    }
}
