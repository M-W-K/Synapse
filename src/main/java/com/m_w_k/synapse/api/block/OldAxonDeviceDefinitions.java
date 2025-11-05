package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.block.ruleset.EnergyTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.FluidTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.ItemTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.connect.EnergyExposer;
import com.m_w_k.synapse.common.connect.FluidExposer;
import com.m_w_k.synapse.common.connect.IEndpointCapability;
import com.m_w_k.synapse.common.connect.ItemExposer;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class OldAxonDeviceDefinitions {

    public static final Reference2IntOpenHashMap<AxonType> STANDARD = new Reference2IntOpenHashMap<>(AxonType.values().length);
    public static final List<AxonType> STANDARD_INV = new ObjectArrayList<>(AxonType.values().length);
    public static final Object2IntOpenHashMap<Pair<AxonType, Direction>> ENDPOINTS =
            new Object2IntOpenHashMap<>(Direction.values().length * AxonType.values().length);
    public static final List<Pair<AxonType, Direction>> ENDPOINTS_INV =
            new ObjectArrayList<>(Direction.values().length * AxonType.values().length);
    public static final Object2IntOpenHashMap<IntObjectPair<AxonType>> RELAYS =
            new Object2IntOpenHashMap<>(Direction.values().length * AxonType.values().length);
    public static final List<IntObjectPair<AxonType>> RELAYS_INV =
            new ObjectArrayList<>(Direction.values().length * 4);

    static {
        AxonType[] oldVals = new AxonType[] { AxonType.ENERGY, AxonType.ITEM, AxonType.FLUID };
        for (AxonType type : oldVals) {
            STANDARD.put(type, type.ordinal());
            STANDARD_INV.add(type.ordinal(), type);
            for (Direction dir : Direction.values()) {
                Pair<AxonType, Direction> pair = Pair.of(type, dir);
                ENDPOINTS.put(pair, dir.ordinal() + Direction.values().length * type.ordinal());
                ENDPOINTS_INV.add(dir.ordinal() + Direction.values().length * type.ordinal(), pair);
            }
            for (int i = 0; i < 4; i++) {
                IntObjectPair<AxonType> pair = IntObjectPair.of(i, type);
                RELAYS.put(pair, i + 4 * type.ordinal());
                RELAYS_INV.add(i + 4 * type.ordinal(), pair);
            }
        }

        STANDARD.defaultReturnValue(-1);
        ENDPOINTS.defaultReturnValue(-1);
        RELAYS.defaultReturnValue(-1);
    }

    public static IntFunction<ResourceLocation> getModernizer(int deviceCount) {
        if (deviceCount == STANDARD_INV.size()) {
            return i -> AxonDeviceDefinitions.STANDARD.get(STANDARD_INV.get(i));
        } else if (deviceCount == ENDPOINTS_INV.size()) {
            return i -> AxonDeviceDefinitions.ENDPOINTS.get(ENDPOINTS_INV.get(i));
        } else if (deviceCount == RELAYS_INV.size()) {
            return i -> AxonDeviceDefinitions.RELAYS.get(RELAYS_INV.get(i));
        } else {
            return null;
        }
    }
}
