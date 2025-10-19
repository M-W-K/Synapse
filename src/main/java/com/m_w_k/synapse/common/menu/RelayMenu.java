package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.IDSetResult;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.network.EndpointRulesetSyncPacket;
import com.m_w_k.synapse.network.SynapsePacketHandler;
import com.m_w_k.synapse.registry.SynapseMenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class RelayMenu extends BasicConnectorMenu {

    public RelayMenu(int containerID, Inventory playerInv, ContainerLevelAccess access, IntFunction<String> deviceNames, int deviceCount) {
        super(SynapseMenuRegistry.RELAY.get(), containerID, playerInv, access, deviceNames, deviceCount);
    }

    public static RelayMenu of(int containerID, Inventory playerInv, IAxonBlockEntity be) {
        RelayMenu menu = new RelayMenu(containerID, playerInv, ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), be::getNameBySlot, be.getSlots());
        menu.be = be;
        return menu;
    }

    public static RelayMenu read(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        int slots = buf.readVarInt();
        String[] names = new String[slots];
        for (int i = 0; i < slots; i++) {
            int length = buf.readVarInt();
            names[i] = buf.readCharSequence(length, Charset.defaultCharset()).toString();
        }
        RelayMenu ret = new RelayMenu(containerID, playerInv, ContainerLevelAccess.NULL, i -> names[i], slots);
        ret.setActiveDevices(buf.readBitSet());
        return ret;
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be) {
        return BasicConnectorMenu.writer(be);
    }
}
