package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.item.AxonItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public abstract class AxonBlock extends BaseEntityBlock {
    public AxonBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                          @NotNull BlockPos pos, @NotNull Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof AxonItem iAxon)) return InteractionResult.PASS;
        BlockEntity b = level.getBlockEntity(pos);
        if (!(b instanceof AxonBlockEntity bAxon)) return InteractionResult.PASS;

        BlockPos connect = iAxon.getConnectPos(stack);
        if (pos.equals(connect)) return InteractionResult.FAIL;
        if (connect == null) {
            iAxon.setConnectPos(stack, pos);
            iAxon.setConnectSlot(stack, 0);
            return InteractionResult.SUCCESS;
        }
        BlockEntity a = level.getBlockEntity(connect);
        if (!(a instanceof AxonBlockEntity aAxon)) return InteractionResult.PASS;

        AxonType type = iAxon.getType();
        int slot = iAxon.getConnectSlot(stack);
        ConnectionTier.Type direction = aAxon.getTier().typeOf(bAxon.getTier());
        if (direction.upstream()) {
            if (aAxon.hasUpstream(type)) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, slot, pos, 0, type, direction);
            if (iAxon.consumeToPlace(connection, stack, player)) {
                aAxon.setUpstream(type, connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.SUCCESS;
            }
        }
        if (direction.downstream()) {
            if (bAxon.hasUpstream(type)) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, 0, connect, slot, type, direction.flip());
            if (iAxon.consumeToPlace(connection, stack, player)) {
                bAxon.setUpstream(type, connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
