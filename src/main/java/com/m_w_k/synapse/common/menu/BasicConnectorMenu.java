package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.IDSetResult;
import com.m_w_k.synapse.common.block.AxonBlock;
import com.m_w_k.synapse.network.*;
import com.m_w_k.synapse.registry.SynapseMenuRegistry;
import it.unimi.dsi.fastutil.booleans.BooleanObjectMutablePair;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class BasicConnectorMenu extends AbstractContainerMenu {
    protected static final int INV_SLOT_START = 0;
    protected static final int INV_SLOT_END = 27;
    protected static final int USE_ROW_SLOT_START = 27;
    protected static final int USE_ROW_SLOT_END = 36;

    protected final ContainerLevelAccess access;
    protected final Map<ResourceLocation, BooleanObjectMutablePair<String>> devices;

    protected @Nullable IAxonBlockEntity be;

    @OnlyIn(Dist.CLIENT)
    protected byte sync;
    @OnlyIn(Dist.CLIENT)
    protected ResourceLocation selectedDevice;
    @OnlyIn(Dist.CLIENT)
    protected short selectedID;
    @OnlyIn(Dist.CLIENT)
    protected AxonAddress selectedAddress;
    @OnlyIn(Dist.CLIENT)
    protected ConnectorLevel selectedLevel;
    @OnlyIn(Dist.CLIENT)
    protected IDSetResult setResult;

    public BasicConnectorMenu(int containerID, Inventory playerInv, ContainerLevelAccess access, Map<ResourceLocation, BooleanObjectMutablePair<String>> devices) {
        this(SynapseMenuRegistry.BASIC_CONNECTOR.get(), containerID, playerInv, access, devices);
    }

    protected BasicConnectorMenu(MenuType<?> type, int containerID, Inventory playerInv, ContainerLevelAccess access, Map<ResourceLocation, BooleanObjectMutablePair<String>> devices) {
        super(type, containerID);
        this.devices = devices;

        this.access = access;
        int i = 36;
        int j = 137;

        for(int k = 0; k < 3; ++k) {
            for(int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInv, l + k * 9 + 9, i + l * 18, j + k * 18));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInv, i1, i + i1 * 18, 195));
        }
    }

    public static BasicConnectorMenu of(int containerID, Inventory playerInv, IAxonBlockEntity be) {
        BasicConnectorMenu menu = new BasicConnectorMenu(containerID, playerInv, ContainerLevelAccess.create(be.level(), be.blockPos()), map(be));
        menu.be = be;
        return menu;
    }

    protected static Object2ObjectOpenHashMap<ResourceLocation, BooleanObjectMutablePair<String>> map(IAxonBlockEntity be) {
        Object2ObjectOpenHashMap<ResourceLocation, BooleanObjectMutablePair<String>> m = new Object2ObjectOpenHashMap<>();
        for (ResourceLocation k : be.getSlots().keySet()) {
            m.put(k, BooleanObjectMutablePair.of(be.slotIsActive(k), be.getNameBySlot(k)));
        }
        return m;
    }

    public static BasicConnectorMenu read(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        return new BasicConnectorMenu(containerID, playerInv, ContainerLevelAccess.NULL, map(buf));
    }

    protected static Map<ResourceLocation, BooleanObjectMutablePair<String>> map(FriendlyByteBuf buf) {
        int slots = buf.readVarInt();
        Map<ResourceLocation, BooleanObjectMutablePair<String>> devices = new Object2ObjectOpenHashMap<>();
        for (int i = 0; i < slots; i++) {
            ResourceLocation key = buf.readResourceLocation();
            String name = buf.readUtf();
            boolean active = buf.readBoolean();
            devices.put(key, BooleanObjectMutablePair.of(active, name));
        }
        return devices;
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be) {
        return buf -> {
            buf.writeVarInt(be.getSlots().size());
            be.getSlots().forEach((resloc, device) -> {
                buf.writeResourceLocation(resloc);
                buf.writeUtf(be.getNameBySlot(resloc));
                buf.writeBoolean(be.slotIsActive(resloc));
            });
        };
    }

    public Map<ResourceLocation, BooleanObjectMutablePair<String>> getDevices() {
        return devices;
    }

    public @NotNull ItemStack quickMoveStack(@NotNull Player p_39051_, int p_39052_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39052_);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (p_39052_ >= INV_SLOT_START && p_39052_ < INV_SLOT_END) {
                if (!this.moveItemStackTo(itemstack1, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (p_39052_ >= USE_ROW_SLOT_START && p_39052_ < USE_ROW_SLOT_END) {
                if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, INV_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_39051_, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() instanceof AxonBlock
                && player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D, true);
    }

    public void sendToClient(ServerPlayer player, ResourceLocation device, IDSetResult result) {
        if (be == null || be.level() == null) return;
        Set<ResourceLocation> active = be.getSlots().keySet().stream().filter(be::slotIsActive).collect(Collectors.toSet());
        ClientboundBasicDeviceDataPacket packet = be.slotIsActive(device) ?
                new ClientboundBasicDeviceDataPacket(active, device, be.getBySlot(device).ensureRegistered(be.level()), result)
                : new ClientboundBasicDeviceDataPacket(active, device, null, ConnectorLevel.CORRUPTED, result);
        SynapsePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void updateID(ServerPlayer player, ResourceLocation device, short id) {
        if (be == null) return;
        IDSetResult result = be.getBySlot(device).setAddressID(id);
        sendToClient(player, device, result);
    }

    @OnlyIn(Dist.CLIENT)
    public void onSync() {
        sync++;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getSync() {
        return sync;
    }

    @OnlyIn(Dist.CLIENT)
    public void setActiveDevices(Set<ResourceLocation> activeDevices) {
        this.devices.forEach((resloc, pair) -> pair.first(activeDevices.contains(resloc)));
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedDevice(ResourceLocation selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    @OnlyIn(Dist.CLIENT)
    public void sendSelectedDevice(ResourceLocation selectedDevice) {
        SynapsePacketHandler.INSTANCE.sendToServer(new ServerboundSetSelectedConnectorPacket(selectedDevice));
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getSelectedDevice() {
        return selectedDevice;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedAddress(AxonAddress selectedAddress) {
        this.selectedAddress = selectedAddress;
    }

    @OnlyIn(Dist.CLIENT)
    public AxonAddress getSelectedAddress() {
        return selectedAddress;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedLevel(ConnectorLevel selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public ConnectorLevel getSelectedLevel() {
        return selectedLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSetResult(IDSetResult setResult) {
        this.setResult = setResult;
    }

    @OnlyIn(Dist.CLIENT)
    public IDSetResult getSetResult() {
        return setResult;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedID(short selectedID) {
        this.selectedID = selectedID;
    }

    @OnlyIn(Dist.CLIENT)
    public short getSelectedID() {
        return selectedID;
    }

    @OnlyIn(Dist.CLIENT)
    public void sendSelectedID(short selectedID) {
        SynapsePacketHandler.INSTANCE.sendToServer(new ServerboundSetConnectorIDPacket(getSelectedDevice(), selectedID));
    }

    @OnlyIn(Dist.CLIENT)
    public void sendRemoveConnection() {
        SynapsePacketHandler.INSTANCE.sendToServer(new ServerboundRemoveConnectionPacket(getSelectedDevice()));
    }

    public void receiveRemoveConnection(ServerPlayer player, ResourceLocation slot) {
        if (be == null || be.removed()) return;
        if (be.removeUpstreamFrom(slot)) {
            sendToClient(player, slot, IDSetResult.NO_SET);
        }
    }
}
