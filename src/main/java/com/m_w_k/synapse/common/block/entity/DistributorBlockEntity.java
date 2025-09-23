package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class DistributorBlockEntity extends AxonBlockEntity {

    public DistributorBlockEntity(BlockPos pos, BlockState state, @NotNull ConnectionTier tier) {
        super(SynapseBlockEntityRegistry.DISTRIBUTOR_BLOCK.get(), pos, state, tier);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.ofSize(getBlockPos().getCenter(), 10, 10, 10);
    }
}
