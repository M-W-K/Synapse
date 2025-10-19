package com.m_w_k.synapse.client.gui;

import com.m_w_k.synapse.common.menu.RelayMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RelayScreen extends AbstractConnectorScreen<RelayMenu> {
    public RelayScreen(RelayMenu menu, Inventory playerInventory, Component p_97743_) {
        super(menu, playerInventory, p_97743_);
    }

    @Override
    protected void updateSelectedDeviceScreen() {
        super.updateSelectedDeviceScreen();
        addressConfig.setVisible(false);
    }
}
