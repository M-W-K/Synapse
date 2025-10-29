package com.m_w_k.synapse;

import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.DeviceDataKey;
import com.m_w_k.synapse.common.block.entity.RelayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

public final class SynapseUtil {

    public static Direction facingTo(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        if (Math.abs(diff.getX()) + Math.abs(diff.getY()) + Math.abs(diff.getZ()) != 1) {
            return null;
        }
        if (Math.abs(diff.getX()) == 1) {
            return Direction.fromAxisAndDirection(Direction.Axis.X, diff.getX() > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        } else if (Math.abs(diff.getY()) == 1) {
            return Direction.fromAxisAndDirection(Direction.Axis.Y, diff.getY() > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        } else {
            return Direction.fromAxisAndDirection(Direction.Axis.Z, diff.getZ() > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        }
    }

    public static @NotNull ConnectorLevel actualLevel(@NotNull ConnectorLevel.Provider provider) {
        var data = provider.getData();
        if (data == null) return provider.getLevel();
        return DeviceDataKey.RELAYING.getFromMap(data, provider.getLevel());
    }

    public static @NotNull ConnectionType actualTypeOf(@NotNull ConnectorLevel.Provider a, @NotNull ConnectorLevel.Provider b) {
        ConnectionType type = actualLevel(a).typeOf(actualLevel(b));
        if (type == ConnectionType.EQUAL && a.getLevel() != b.getLevel()) {
            return a.getLevel().typeOf(b.getLevel());
        }
        return type;
    }

    public static <T> T getNearest(T target, T[] candidates, ToDoubleBiFunction<T, T> distance) {
        int best = 0;
        double dist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < candidates.length; i++) {
            double d = distance.applyAsDouble(target, candidates[i]);
            if (dist > d) {
                best = i;
                dist = d;
            }
        }
        return candidates[best];
    }

    public static <T> T getNearest(Vec3 vec, T[] candidates, Function<T, Vec3> toVec) {
        int best = 0;
        double dist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < candidates.length; i++) {
            double d = distanceEuler(vec, toVec.apply(candidates[i]));
            if (dist > d) {
                best = i;
                dist = d;
            }
        }
        return candidates[best];
    }

    public static int getNearest(Vec3 vec, Vec3[] candidates) {
        int best = 0;
        double dist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < candidates.length; i++) {
            double d = distanceEuler(vec, candidates[i]);
            if (dist > d) {
                best = i;
                dist = d;
            }
        }
        return best;
    }

    public static double distanceEuler(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public static ResourceLocation resLoc(String path) {
        return new ResourceLocation(SynapseMod.MODID, path);
    }
}
