package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import it.unimi.dsi.fastutil.booleans.BooleanObjectMutablePair;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class InitFilterMenu extends BasicConnectorMenu {

    @OnlyIn(Dist.CLIENT)
    protected String startingFilter;

    public InitFilterMenu(int containerID, Inventory playerInv, ContainerLevelAccess access,
                          Map<ResourceLocation, BooleanObjectMutablePair<String>> devices) {
        super(containerID, playerInv, access, devices);
    }

    protected InitFilterMenu(MenuType<?> type, int containerID, Inventory playerInv, ContainerLevelAccess access,
                             Map<ResourceLocation, BooleanObjectMutablePair<String>> devices) {
        super(type, containerID, playerInv, access, devices);
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be, @NotNull String startingFilter) {

        return buf -> {
            buf.writeVarInt(be.getSlots().size());
            be.getSlots().forEach((resloc, device) -> {
                buf.writeResourceLocation(resloc);
                buf.writeUtf(be.getNameBySlot(resloc));
                buf.writeBoolean(be.slotIsActive(resloc));
            });
            buf.writeUtf(startingFilter);
        };
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be) {
        return writer(be, "");
    }

    protected <T extends InitFilterMenu> T readStartingFilter(FriendlyByteBuf buf) {
        this.startingFilter = buf.readUtf();
        return (T) this;
    }

    @OnlyIn(Dist.CLIENT)
    public String getStartingFilter() {
        return startingFilter;
    }
}
