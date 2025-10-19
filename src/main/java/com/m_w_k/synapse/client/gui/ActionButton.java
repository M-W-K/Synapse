package com.m_w_k.synapse.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class ActionButton extends AbstractButton {
    private final @NotNull TexLocation texLocation;

    private final BooleanSupplier isActive;
    private final Runnable onPress;
    private final TexDefinition[] definitions;

    public ActionButton(@NotNull TexLocation texLocation, int x, int y, int size, Component p_93369_,
                        BooleanSupplier isActive, Runnable onPress,
                        TexDefinition... definitions) {
        this(texLocation, x, y, size, size, p_93369_, isActive, onPress, definitions);
    }

    public ActionButton(@NotNull TexLocation texLocation, int x, int y, int width, int height, Component p_93369_,
                        BooleanSupplier isActive, Runnable onPress,
                        TexDefinition... definitions) {
        super(x, y, width, height, p_93369_);
        this.texLocation = texLocation;
        this.isActive = isActive;
        this.onPress = onPress;
        this.definitions = definitions;
        setTooltip(Tooltip.create(p_93369_));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        boolean isActive = this.isActive.getAsBoolean();
        boolean isHovered = this.isHovered();
        for (TexDefinition definition : definitions) {
            graphics.blit(texLocation.loc(), getX(), getY(), 0,
                    definition.x(isActive, isHovered), definition.y(isActive, isHovered),
                    width, height, texLocation.xSize(), texLocation.ySize());
        }
    }

    @Override
    public void onPress() {
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
    }

    public record ButtonFactory(@NotNull TexLocation texLocation) {

        public @NotNull ActionButton button(int x, int y, int size, Component p_93369_,
                                                BooleanSupplier isActive, Runnable onPress,
                                                TexDefinition... definitions) {
            return new ActionButton(texLocation, x, y, size, p_93369_, isActive, onPress, definitions);
        }

        public @NotNull ActionButton button(int x, int y, int width, int height, Component p_93369_,
                                                BooleanSupplier isActive, Runnable onPress,
                                                TexDefinition... definitions) {
            return new ActionButton(texLocation, x, y, width, height, p_93369_, isActive, onPress, definitions);
        }
    }
}
