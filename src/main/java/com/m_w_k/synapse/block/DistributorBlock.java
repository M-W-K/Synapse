package com.m_w_k.synapse.block;

import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.block.entity.DistributorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistributorBlock extends BaseEntityBlock {
    private final @NotNull ConnectionTier tier;

    public DistributorBlock(Properties p_49795_, @NotNull ConnectionTier tier) {
        super(p_49795_);
        this.tier = tier;
    }

    @Override
    public @NotNull DistributorBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new DistributorBlockEntity(p_153215_, p_153216_, tier);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }
}
