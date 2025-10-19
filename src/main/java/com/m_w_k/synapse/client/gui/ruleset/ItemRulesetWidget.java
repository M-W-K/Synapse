package com.m_w_k.synapse.client.gui.ruleset;

import com.google.common.collect.ImmutableList;
import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.ruleset.ItemRuleAccess;
import com.m_w_k.synapse.api.block.ruleset.ItemTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.RuleAction;
import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.client.gui.AbstractConnectorScreen;
import com.m_w_k.synapse.client.gui.ActionButton;
import com.m_w_k.synapse.client.gui.TexDefinition;
import com.m_w_k.synapse.client.gui.TexLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemRulesetWidget extends AbstractContainerEventHandler implements RulesetWidget {
    static final TexLocation TEX_LOCATION = new TexLocation(SynapseUtil.resLoc("textures/gui/container/ruleset.png"), 128, 128);

    protected final ItemTransferRuleset parent;
    protected final AbstractConnectorScreen<?> screen;
    private final int x;
    private final int y;

    protected int currentRule = 0;
    protected final ActionButton left;
    protected final ActionButton right;
    protected final ActionButton swapLeft;
    protected final ActionButton swapRight;
    protected final ActionButton delete;
    protected final ActionButton add;

    protected @NotNull String lastAddress = "";
    protected final EditBox addressBox;

    protected final ItemSlot[] filterSlots = new ItemSlot[9];

    protected final ActionButton nbtMatch;
    protected final ActionButton whitelist;
    protected final ActionButton filterIncoming;
    protected final ActionButton filterOutgoing;

    protected final List<AbstractWidget> children;

    public ItemRulesetWidget(ItemTransferRuleset parent, AbstractConnectorScreen<?> screen, int x, int y) {
        this.parent = parent;
        this.screen = screen;
        this.x = x;
        this.y = y;

        ImmutableList.Builder<AbstractWidget> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                filterSlots[3 * i + j] = new ItemSlot(x + 58 + 18*i, y + 31 + 18*j,
                        Component.translatable("synapse.menu.button.ruleset.item"), 3 * i + j);
                builder.add(filterSlots[3 * i + j]);
            }
        }
        ActionButton.ButtonFactory factory = new ActionButton.ButtonFactory(TEX_LOCATION);
        builder.add(left = factory.button(x, y, 11, Component.translatable("synapse.menu.button.ruleset.left"),
                () -> currentRule > 0, () -> {
            currentRule = Math.max(0, currentRule - 1);
            onRuleChanged();
        },
                TexDefinition.noHoverInactive(11, 44, 0, 44, 22, 44),
                TexDefinition.simple(0, 55)));
        builder.add(right = factory.button(x + 15, y, 11, Component.translatable("synapse.menu.button.ruleset.right"),
                () -> currentRule < parent.ruleCount() - 1, () -> {
            currentRule = Math.min(parent.ruleCount() - 1, currentRule + 1);
            onRuleChanged();
        },
                TexDefinition.noHoverInactive(11, 44, 0, 44, 22, 44),
                TexDefinition.simple(11, 55)));
        builder.add(swapLeft = factory.button(x + 65 + 1, y, 11, Component.translatable("synapse.menu.button.ruleset.swap_left"),
                () -> currentRule > 0, () -> {
            if (currentRule == 0) return;
            parent.applyAction(currentRule, RuleAction.SHIFT_LEFT);
            currentRule--;
            onRuleChanged();
        },
                TexDefinition.noHoverInactive(11, 44, 0, 44, 22, 44),
                TexDefinition.simple(0, 66)));
        builder.add(swapRight = factory.button(x + 80 + 1, y, 11, Component.translatable("synapse.menu.button.ruleset.swap_right"),
                () -> currentRule < parent.ruleCount() - 1, () -> {
            if (currentRule >= parent.ruleCount() - 1) return;
            parent.applyAction(currentRule, RuleAction.SHIFT_RIGHT);
            currentRule++;
            onRuleChanged();
        },
                TexDefinition.noHoverInactive(11, 44, 0, 44, 22, 44),
                TexDefinition.simple(11, 66)));
        builder.add(delete = factory.button(x + 95 + 1, y, 11, Component.translatable("synapse.menu.button.ruleset.delete"),
                () -> true /* replace with check to see if the rule is not the default rule */, () -> {
            parent.applyAction(currentRule, RuleAction.DELETE);
            if (currentRule == parent.ruleCount()) currentRule--;
            onRuleChanged();
        },
                TexDefinition.noHoverInactive(11, 44, 0, 44, 22, 44),
                TexDefinition.simple(0, 77)));
        builder.add(add = factory.button(x + 110 + 1, y, 11, Component.translatable("synapse.menu.button.ruleset.add"),
                () -> parent.ruleCount() < 50, () -> {
            if (parent.ruleCount() >= 50) return;
            currentRule += 1;
            parent.applyAction(currentRule, RuleAction.ADD);
            onRuleChanged();
        },
                TexDefinition.noHoverInactive(11, 44, 0, 44, 22, 44),
                TexDefinition.simple(11, 77)));

        builder.add(addressBox = new EditBox(screen.getFontRenderer(), x, y + 15, 120, 14, Component.translatable("synapse.menu.ruleset.address_config")));

        builder.add(nbtMatch = factory.button(x, y + 35, 22, Component.translatable("synapse.menu.button.ruleset.nbt"),
                () -> currentRule().isMatchNBT(), () -> currentRule().setMatchNBT(!currentRule().isMatchNBT()),
                new TexDefinition(0, 0, 22, 0, 0, 22, 22, 22),
                TexDefinition.active(44, 0, 66, 0)));
        builder.add(whitelist = factory.button(x, y + 60, 22, Component.translatable("synapse.menu.button.ruleset.whitelist"),
                () -> currentRule().isWhitelist(), () -> currentRule().setWhitelist(!currentRule().isWhitelist()),
                TexDefinition.hover(0, 0, 0, 22),
                TexDefinition.active(44, 22, 66, 22)));
        builder.add(filterIncoming = factory.button(x + 25, y + 35, 22, Component.translatable("synapse.menu.button.ruleset.incoming"),
                () -> currentRule().isMatchesIncoming(), () -> currentRule().setMatchesIncoming(!currentRule().isMatchesIncoming()),
                new TexDefinition(0, 0, 22, 0, 0, 22, 22, 22),
                TexDefinition.active(44, 44, 66, 44)));
        builder.add(filterOutgoing = factory.button(x + 25, y + 60, 22, Component.translatable("synapse.menu.button.ruleset.outgoing"),
                () -> currentRule().isMatchesOutgoing(), () -> currentRule().setMatchesOutgoing(!currentRule().isMatchesOutgoing()),
                new TexDefinition(0, 0, 22, 0, 0, 22, 22, 22),
                TexDefinition.active(44, 66, 66, 66)));
        children = builder.build();

        addressBox.setMaxLength(50);
    }

    protected ItemRuleAccess currentRule() {
        return parent.ruleAt(currentRule);
    }

    @Override
    public void updateSelectedDevice() {
        onRuleChanged();
    }

    protected void onRuleChanged() {
        addressBox.setValue(currentRule().getAddress().toString());
    }

    @Override
    public void containerTick() {
        addressBox.tick();
        if (!lastAddress.equals(addressBox.getValue())) {
            lastAddress = addressBox.getValue();
            AxonAddress.parse(lastAddress).ifLeft(a -> currentRule().setAddress(a));
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        for (var child : children) {
            child.render(graphics, mouseX, mouseY, partial);
            String s = String.valueOf(currentRule + 1);
            graphics.drawString(screen.getFontRenderer(), s + " / " + parent.ruleCount(), x + 39 - screen.getFontRenderer().width(s), y + 2, 0xFFFFFF, false);
        }
    }

    public NarratableEntry.@NotNull NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        } else {
            return NarratableEntry.NarrationPriority.NONE;
        }
    }

    public final void updateNarration(@NotNull NarrationElementOutput p_259921_) {}

    protected class ItemSlot extends AbstractWidget {
        protected int index;

        public ItemSlot(int x, int y, Component p_93633_, int index) {
            super(x, y, 18, 18, p_93633_);
            this.index = index;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
            graphics.blit(TEX_LOCATION.loc(), getX(), getY(), 0, 88, 0, 18, 18, TEX_LOCATION.xSize(), TEX_LOCATION.ySize());
            ItemStack stack = currentRule().getMatchStack(index);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, getX() + 1, getY() + 1);
                graphics.renderItemDecorations(screen.getFontRenderer(), stack, getX() + 1, getY() + 1);
            }
            if (isHovered()) {
                AbstractContainerScreen.renderSlotHighlight(graphics, getX() + 1, getY() + 1, 0, -2130706433);
                if (!stack.isEmpty() && screen.getMenu().getCarried().isEmpty()) {
                    graphics.renderTooltip(screen.getFontRenderer(), AbstractContainerScreen.getTooltipFromItem(screen.getMinecraftInstance(), stack), stack.getTooltipImage(), stack, mouseX, mouseY);
                }
            }
        }

        @Override
        public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
            if (!isHovered()) return false;
            if (p_93643_ == 0) {
                currentRule().setMatchStack(index, screen.getMenu().getCarried().copyWithCount(1));
            } else {
                currentRule().setMatchStack(index, ItemStack.EMPTY);
            }
            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput p_259858_) {}
    }

}
