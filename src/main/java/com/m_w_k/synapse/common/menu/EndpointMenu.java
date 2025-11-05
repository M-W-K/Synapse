package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.IDSetResult;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.network.EndpointRulesetSyncPacket;
import com.m_w_k.synapse.network.SynapsePacketHandler;
import com.m_w_k.synapse.registry.SynapseMenuRegistry;
import it.unimi.dsi.fastutil.booleans.BooleanObjectMutablePair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class EndpointMenu extends InitFilterMenu {

    @OnlyIn(Dist.CLIENT)
    protected TransferRuleset selectedRuleset;

    public EndpointMenu(int containerID, Inventory playerInv, ContainerLevelAccess access,
                        Map<ResourceLocation, BooleanObjectMutablePair<String>> devices) {
        super(SynapseMenuRegistry.ENDPOINT.get(), containerID, playerInv, access, devices);
    }

    public static EndpointMenu of(int containerID, Inventory playerInv, IAxonBlockEntity be) {
        EndpointMenu menu = new EndpointMenu(containerID, playerInv, ContainerLevelAccess.create(be.level(), be.blockPos()), map(be));
        menu.be = be;
        return menu;
    }

    public static EndpointMenu read(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        return new EndpointMenu(containerID, playerInv, ContainerLevelAccess.NULL, map(buf)).readStartingFilter(buf);
    }

    @Override
    public void sendToClient(ServerPlayer player, ResourceLocation device, IDSetResult result) {
        super.sendToClient(player, device, result);
        TransferRuleset ruleset = getRulesetServerside(device);
        if (ruleset == null) return;
        SynapsePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new EndpointRulesetSyncPacket(ruleset.getType(), ruleset.clientSyncData(), Dist.CLIENT, device));
    }

    public TransferRuleset getRulesetServerside(ResourceLocation device) {
        if (be == null || be.level() == null || !(be instanceof EndpointBlockEntity endpoint)) return null;
        if (be.slotIsActive(device)) {
            return endpoint.rulesetForSlot(device);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setSelectedDevice(ResourceLocation selectedDevice) {
        super.setSelectedDevice(selectedDevice);
        setSelectedRuleset(null);
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedRuleset(TransferRuleset selectedRuleset) {
        this.selectedRuleset = selectedRuleset;
    }

    @OnlyIn(Dist.CLIENT)
    public TransferRuleset getSelectedRuleset() {
        return selectedRuleset;
    }

    @OnlyIn(Dist.CLIENT)
    public void sendRulesetSync(Consumer<FriendlyByteBuf> sync) {
        TransferRuleset ruleset = getSelectedRuleset();
        if (ruleset == null) return;
        SynapsePacketHandler.INSTANCE.sendToServer(new EndpointRulesetSyncPacket(ruleset.getType(), sync, Dist.DEDICATED_SERVER, selectedDevice));
    }
}
