package com.m_w_k.synapse.client.gui;

public record TexDefinition(int xActive, int yActive, int xInactive, int yInactive,
                     int xActiveHovered, int yActiveHovered, int xInactiveHovered, int yInactiveHovered) {

    public static TexDefinition simple(int x, int y) {
        return new TexDefinition(x, y, x, y, x, y, x, y);
    }

    public static TexDefinition active(int xActive, int yActive, int xInactive, int yInactive) {
        return new TexDefinition(xActive, yActive, xInactive, yInactive, xActive, yActive, xInactive, yInactive);
    }

    public static TexDefinition hover(int x, int y, int xHover, int yHover) {
        return new TexDefinition(x, y, x, y, xHover, yHover, xHover, yHover);
    }

    public static TexDefinition noHoverInactive(int xActive, int yActive, int xInactive, int yInactive, int xHover, int yHover) {
        return new TexDefinition(xActive, yActive, xInactive, yInactive, xHover, yHover, xInactive, yInactive);
    }

    public int x(boolean active, boolean hovered) {
        if (active) {
            if (hovered) {
                return xActiveHovered;
            } else {
                return xActive;
            }
        } else {
            if (hovered) {
                return xInactiveHovered;
            } else {
                return xInactive;
            }
        }
    }

    public int y(boolean active, boolean hovered) {
        if (active) {
            if (hovered) {
                return yActiveHovered;
            } else {
                return yActive;
            }
        } else {
            if (hovered) {
                return yInactiveHovered;
            } else {
                return yInactive;
            }
        }
    }
}
