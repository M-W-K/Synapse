package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.KnifeAction;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.OldAxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.common.item.KnifeItem;
import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import com.m_w_k.synapse.config.SynapseConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Set;

public abstract class AxonBlock extends BaseEntityBlock {
    public AxonBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                          @NotNull BlockPos pos, @NotNull Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        BlockEntity b = level.getBlockEntity(pos);
        if (!(b instanceof IAxonBlockEntity usAxon)) return InteractionResult.PASS;

        if (!(stack.getItem() instanceof AxonItem iAxon)) {
            if (stack.getItem() instanceof KnifeItem knife) {
                return handleKnife(state, level, pos, player, hand, hit, usAxon, knife, stack);
            }
            if (noMenuItem(stack)) return InteractionResult.PASS;
            if (hand == InteractionHand.OFF_HAND || !hasInteractMenu()) return InteractionResult.PASS;
            if (player instanceof ServerPlayer s) {
                openInteractMenu(s, level, state, pos, usAxon, hit);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockPos connect = iAxon.getConnectPos(stack);
        if (pos.equals(connect)) return InteractionResult.FAIL;
        ResourceLocation usSlot = determineHitSlot(state, level, pos, player, hand, hit);
        if (usSlot == null || !usAxon.slotIsActive(usSlot)) return InteractionResult.FAIL;
        if (connect == null) {
            iAxon.setConnectPos(stack, pos);
            iAxon.setConnectSlot(stack, usSlot);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (connect.distSqr(pos) > SynapseConfigs.server().network.getSquareRangeLimit()) {
            return InteractionResult.FAIL;
        }
        BlockEntity a = level.getBlockEntity(connect);
        if (!(a instanceof IAxonBlockEntity themAxon)) return InteractionResult.PASS;

        AxonType type = iAxon.getType();
        ResourceLocation themSlot = iAxon.getConnectSlot(stack);
        if (!themAxon.hasSlot(themSlot)) {
            iAxon.clearConnectData(stack);
            return InteractionResult.FAIL;
        }
        LocalConnectorDevice us = usAxon.getBySlot(usSlot);
        if (us.type() != type) {
            iAxon.clearConnectData(stack);
            return InteractionResult.FAIL;
        }
        LocalConnectorDevice them = themAxon.getBySlot(themSlot);
        if (them.type() != type) {
            iAxon.clearConnectData(stack);
            return InteractionResult.FAIL;
        }
        ConnectionType direction = SynapseUtil.actualTypeOf(us, them);
        us.ensureRegistered(level);
        them.ensureRegistered(level);
        if (direction.upstream()) {
            if (us.upstream() != null || !usAxon.allowsUpstream(usSlot, them) ||
                    !themAxon.allowsDownstream(themSlot, us)) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, usSlot,
                    randOffset(usAxon.renderOffsetForSlot(usSlot, themAxon), 40),
                    usAxon.renderDirectionForSlot(usSlot, themAxon),
                    connect, themSlot,
                    randOffset(themAxon.renderOffsetForSlot(themSlot, usAxon), 40),
                    themAxon.renderDirectionForSlot(themSlot, usAxon),
                    type, direction);
            if (iAxon.consumeToPlace(connection, stack, player, false)) {
                var tree = AxonTree.load(level, type, type.getCapability());
                if (tree.isEmpty() || tree.get().connect(us.treeID(), null, them.treeID(), null) == null) {
                    return InteractionResult.FAIL;
                }
                iAxon.consumeToPlace(connection, stack, player, true);
                usAxon.setUpstream(connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else if (direction.downstream()) {
            if (them.upstream() != null || !themAxon.allowsUpstream(themSlot, us) ||
                    !usAxon.allowsDownstream(usSlot, them)) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, themSlot,
                    randOffset(themAxon.renderOffsetForSlot(themSlot, usAxon), 40),
                    themAxon.renderDirectionForSlot(themSlot, usAxon),
                    pos, usSlot,
                    randOffset(usAxon.renderOffsetForSlot(usSlot, themAxon), 40),
                    usAxon.renderDirectionForSlot(usSlot, themAxon),
                    type, direction.flip());
            if (iAxon.consumeToPlace(connection, stack, player, false)) {
                var tree = AxonTree.load(level, type, type.getCapability());
                if (tree.isEmpty() || tree.get().connect(us.treeID(), null, them.treeID(), null) == null) {
                    return InteractionResult.FAIL;
                }
                iAxon.consumeToPlace(connection, stack, player, true);
                themAxon.setUpstream(connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    protected boolean noMenuItem(ItemStack stack) {
        return false;
    }

    private Vec3 randOffset(Vec3 vec, int inv) {
        return vec.add((Math.random() - 0.5) / inv, (Math.random() - 0.5) / inv, (Math.random() - 0.5) / inv);
    }

    protected boolean hasInteractMenu() {
        return true;
    }

    protected void openInteractMenu(@NotNull ServerPlayer player, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull IAxonBlockEntity be, @NotNull BlockHitResult hit) {
        MenuProvider prov = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> BasicConnectorMenu.of(containerId, playerInventory, be),
                Component.translatable("synapse.menu.title.basic_connector"));
        NetworkHooks.openScreen(player, prov, BasicConnectorMenu.writer(be));
    }

    protected InteractionResult handleKnife(@NotNull BlockState state, @NotNull Level level,
                               @NotNull BlockPos pos, @NotNull Player player,
                               @NotNull InteractionHand hand, @NotNull BlockHitResult hit,
                               @NotNull IAxonBlockEntity usAxon, @NotNull KnifeItem knife,
                                            @NotNull ItemStack knifeStack) {
        if (knife.getAction() == KnifeAction.NONE) return InteractionResult.PASS;
        Set<ResourceLocation> slots = knifeAffectedSlots(state, level, pos, player, hand, hit, usAxon, knife, knifeStack);
        if (slots == null) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        } else {
            if (slots.isEmpty()) return InteractionResult.FAIL;
            for (ResourceLocation resloc : slots) {
                switch (knife.getAction()) {
                    case SEVER -> {
                        usAxon.removeUpstreamFrom(resloc);
                        usAxon.removeDownstreamFrom(resloc);
                    }
                    case REMOVE -> usAxon.retireSlot(resloc);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // note -- only return null if the block should be removed by this knife action, otherwise return an empty set.
    protected @Nullable Set<ResourceLocation> knifeAffectedSlots(@NotNull BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player,
                                                       @NotNull InteractionHand hand, @NotNull BlockHitResult hit,
                                                       @NotNull IAxonBlockEntity usAxon, @NotNull KnifeItem knife,
                                                       @NotNull ItemStack knifeStack) {
        return AxonDeviceDefinitions.STANDARD.values();
    }

    protected @Nullable ResourceLocation determineHitSlot(@NotNull BlockState state, @NotNull Level level,
                                                          @NotNull BlockPos pos, @NotNull Player player,
                                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof AxonItem iAxon) {
            return AxonDeviceDefinitions.standard(iAxon.getType(), false);
        }
        return null;
    }
}
