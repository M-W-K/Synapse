package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.registry.SynapseMenuRegistry;
import it.unimi.dsi.fastutil.booleans.BooleanObjectMutablePair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.IntFunction;

public class RelayMenu extends InitFilterMenu {

    @OnlyIn(Dist.CLIENT)
    protected String startingFilter;

    public RelayMenu(int containerID, Inventory playerInv, ContainerLevelAccess access, Map<ResourceLocation, BooleanObjectMutablePair<String>> devices) {
        super(SynapseMenuRegistry.RELAY.get(), containerID, playerInv, access, devices);
    }

    public static RelayMenu of(int containerID, Inventory playerInv, IAxonBlockEntity be) {
        RelayMenu menu = new RelayMenu(containerID, playerInv, ContainerLevelAccess.create(be.level(), be.blockPos()), map(be));
        menu.be = be;
        return menu;
    }

    public static RelayMenu read(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        return new RelayMenu(containerID, playerInv, ContainerLevelAccess.NULL, map(buf)).readStartingFilter(buf);
    }
}
