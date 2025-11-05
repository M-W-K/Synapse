package com.m_w_k.synapse.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DeviceListWidget extends ObjectSelectionList<DeviceListWidget.DeviceEntry> {

    protected final AbstractConnectorScreen<?> parent;
    protected final int listWidth;

    public DeviceListWidget(AbstractConnectorScreen<?> parent, int left, int listWidth, int top, int bottom) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, top, bottom, parent.getFontRenderer().lineHeight + 8);
        this.x0 = left;
        this.x1 = left + listWidth;
        this.parent = parent;
        this.listWidth = listWidth;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.refreshList();
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        parent.getMenu().getDevices().forEach((resloc, pair) -> {
            if (parent.displayDevice(resloc)) {
                this.addEntry(new DeviceEntry(resloc, pair.right()));
            }
        });
    }

    public class DeviceEntry extends ObjectSelectionList.Entry<DeviceEntry> {

        protected final @NotNull ResourceLocation slot;
        protected final @NotNull String deviceName;

        protected DeviceEntry(@NotNull ResourceLocation slot, @NotNull String deviceName) {
            this.slot = slot;
            this.deviceName = deviceName;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(deviceName);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
//            graphics.fill(-top, top, -left, left, 0xFFFFFF);
            graphics.drawString(parent.getFontRenderer(), Language.getInstance().getVisualOrder(FormattedText.composite(parent.getFontRenderer().substrByWidth(Component.literal(deviceName), listWidth))), left + 3, top + 2, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            parent.setSelected(this);
            DeviceListWidget.this.setSelected(this);
            return false;
        }

        public @NotNull ResourceLocation getSlot() {
            return slot;
        }

        public String getDeviceName() {
            return deviceName;
        }
    }
}
