package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class InitFilterMenu extends BasicConnectorMenu {

    @OnlyIn(Dist.CLIENT)
    protected String startingFilter;

    public InitFilterMenu(int containerID, Inventory playerInv, ContainerLevelAccess access, IntFunction<String> deviceNames, int deviceCount) {
        super(containerID, playerInv, access, deviceNames, deviceCount);
    }

    protected InitFilterMenu(MenuType<?> type, int containerID, Inventory playerInv, ContainerLevelAccess access, IntFunction<String> deviceNames, int deviceCount) {
        super(type, containerID, playerInv, access, deviceNames, deviceCount);
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be, @NotNull String startingFilter) {
        return buf -> {
            buf.writeVarInt(be.getSlots());
            for (int i = 0; i < be.getSlots(); i++) {
                String name = be.getNameBySlot(i);
                buf.writeVarInt(name.length());
                buf.writeCharSequence(name, Charset.defaultCharset());
            }
            buf.writeBitSet(evaluateActiveness(be));
            writeStartingFilter(buf, startingFilter);
        };
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be) {
        return writer(be, "");
    }

    protected void readStartingFilter(FriendlyByteBuf buf) {
        int len = buf.readVarInt();
        this.startingFilter = buf.readCharSequence(len, Charset.defaultCharset()).toString();
    }

    protected static void writeStartingFilter(FriendlyByteBuf buf, String startingFilter) {
        buf.writeVarInt(startingFilter.length());
        buf.writeCharSequence(startingFilter, Charset.defaultCharset());
    }

    @OnlyIn(Dist.CLIENT)
    public String getStartingFilter() {
        return startingFilter;
    }
}
