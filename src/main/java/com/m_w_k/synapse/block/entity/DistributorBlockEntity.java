package com.m_w_k.synapse.block.entity;

import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class DistributorBlockEntity extends BlockEntity {

    public DistributorBlockEntity(BlockPos p_155229_, BlockState p_155230_, ConnectionTier tier) {
        super(SynapseBlockEntityRegistry.DISTRIBUTOR_BLOCK.get(), p_155229_, p_155230_);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return super.getUpdateTag();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.ofSize(getBlockPos().getCenter(), 10, 10, 10);
    }
}
