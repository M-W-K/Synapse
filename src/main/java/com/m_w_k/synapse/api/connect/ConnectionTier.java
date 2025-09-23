package com.m_w_k.synapse.api.connect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;
import org.jetbrains.annotations.NotNull;

public enum ConnectionTier implements StringRepresentable, IExtensibleEnum {
    ENDPOINT(Double.NEGATIVE_INFINITY), RELAY(Double.NaN),
    DISTRIBUTOR_1(1), DISTRIBUTOR_2(2), DISTRIBUTOR_3(3);

    public static final ConnectionTier.Type[] CONNECTION_TYPES = Type.values();

    public static final Codec<ConnectionTier> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ConnectionTier::values, ConnectionTier::valueOf);

    private final double prio;

    ConnectionTier(double prio) {
        this.prio = prio;
    }

    public static ConnectionTier create(String name, double prio) {
        throw new IllegalStateException("Enum not extended");
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }

    public double getPrio() {
        return prio;
    }

    /**
     * Gets the relative connection type to another tier
     * @param other the other tier
     * @return the connection type of any connection that runs from this to {@code other}.
     */
    public @NotNull Type typeOf(@NotNull ConnectionTier other) {
        if (Double.isFinite(this.getPrio()) && Double.isFinite(other.getPrio())) {
            if (this.getPrio() == other.getPrio()) {
                return Type.EQUAL;
            } else if (this.getPrio() < other.getPrio()) {
                return Type.UPSTREAM;
            }
            return Type.DOWNSTREAM;
        }
        if (this.getPrio() < other.getPrio()) {
            return Type.UNKNOWN_UP;
        } else if (this.getPrio() > other.getPrio()) {
            return Type.UNKNOWN_DOWN;
        }
        return Type.UNKNOWN;
    }

    public enum Type {
        UPSTREAM, DOWNSTREAM, EQUAL, UNKNOWN_UP, UNKNOWN_DOWN, UNKNOWN;

        public boolean upstream() {
            return this == UPSTREAM || this == UNKNOWN_UP;
        }

        public boolean downstream() {
            return this == DOWNSTREAM || this == UNKNOWN_DOWN;
        }

        public @NotNull Type flip() {
            return switch (this) {
                case UPSTREAM -> DOWNSTREAM;
                case DOWNSTREAM -> UPSTREAM;
                case UNKNOWN_UP -> UNKNOWN_DOWN;
                case UNKNOWN_DOWN -> UNKNOWN_UP;
                default -> this;
            };
        }
    }
}
