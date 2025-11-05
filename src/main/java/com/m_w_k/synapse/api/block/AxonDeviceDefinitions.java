package com.m_w_k.synapse.api.block;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.function.Function;

public class AxonDeviceDefinitions {

    public static final BiMap<AxonType, ResourceLocation> STANDARD = HashBiMap.create(AxonType.values().length);
    public static final BiMap<Pair<AxonType, Direction>, ResourceLocation> ENDPOINTS =
            HashBiMap.create(Direction.values().length * AxonType.values().length);
    public static final BiMap<IntObjectPair<AxonType>, ResourceLocation> RELAYS =
            HashBiMap.create(Direction.values().length * AxonType.values().length);

    public static final Reference2ObjectOpenHashMap<AxonType, TransferRuleset> ENDPOINT_RULES =
            new Reference2ObjectOpenHashMap<>(AxonType.values().length);
    public static final Reference2ObjectOpenHashMap<AxonType, Function<IFacedAxonBlockEntity, IEndpointCapability>> ENDPOINT_CAPABILITIES =
            new Reference2ObjectOpenHashMap<>(AxonType.values().length);

    static {
        for (AxonType type : AxonType.values()) {
            STANDARD.put(type, type.getResloc());
            for (Direction dir : Direction.values()) {
                Pair<AxonType, Direction> pair = Pair.of(type, dir);
                ENDPOINTS.put(pair, type.getResloc().withSuffix("_" + dir.getName()));
            }
            for (int i = 0; i < 4; i++) {
                IntObjectPair<AxonType> pair = IntObjectPair.of(i, type);
                RELAYS.put(pair, type.getResloc().withSuffix("_" + i));
            }
        }
        ENDPOINT_RULES.put(AxonType.ITEM, new ItemTransferRuleset(Dist.DEDICATED_SERVER));
        ENDPOINT_RULES.put(AxonType.FLUID, new FluidTransferRuleset(Dist.DEDICATED_SERVER));
        ENDPOINT_RULES.put(AxonType.ENERGY, new EnergyTransferRuleset(Dist.DEDICATED_SERVER));
        ENDPOINT_CAPABILITIES.put(AxonType.ITEM, ItemExposer::new);
        ENDPOINT_CAPABILITIES.put(AxonType.FLUID, FluidExposer::new);
        ENDPOINT_CAPABILITIES.put(AxonType.ENERGY, EnergyExposer::new);
    }

    @Contract("_,true->!null")
    public static @Nullable ResourceLocation standard(@NotNull AxonType type, boolean throwIfNull) {
        if (throwIfNull && !STANDARD.containsKey(type)) {
            throw new IllegalArgumentException("Attempted to get the resloc for a nonexistent definition!");
        }
        return STANDARD.get(type);
    }

    @Contract("_,true->!null")
    public static @Nullable AxonType standard(@NotNull ResourceLocation resloc, boolean throwIfNull) {
        if (throwIfNull && !STANDARD.inverse().containsKey(resloc)) {
            throw new IllegalArgumentException("Attempted to get a nonexistent definition for a resloc!");
        }
        return STANDARD.inverse().get(resloc);
    }

    @Contract("_,_,true->!null")
    public static @Nullable ResourceLocation endpoint(@NotNull AxonType type, @NotNull Direction direction, boolean throwIfNull) {
        var key = Pair.of(type, direction);
        if (throwIfNull && !ENDPOINTS.containsKey(key)) {
            throw new IllegalArgumentException("Attempted to get the resloc for a nonexistent definition!");
        }
        return ENDPOINTS.get(key);
    }

    @Contract("_,true->!null")
    public static @Nullable Pair<AxonType, Direction> endpoint(@NotNull ResourceLocation resloc, boolean throwIfNull) {
        if (throwIfNull && !ENDPOINTS.inverse().containsKey(resloc)) {
            throw new IllegalArgumentException("Attempted to get a nonexistent definition for a resloc!");
        }
        return ENDPOINTS.inverse().get(resloc);
    }

    @Contract("_,_,true->!null")
    public static @Nullable ResourceLocation relay(@NotNull AxonType type, @Range(from = 0, to = 3) int index, boolean throwIfNull) {
        var key = IntObjectPair.of(index, type);
        if (throwIfNull && !RELAYS.containsKey(key)) {
            throw new IllegalArgumentException("Attempted to get the resloc for a nonexistent definition!");
        }
        return RELAYS.get(key);
    }

    @Contract("_,true->!null")
    public static @Nullable IntObjectPair<AxonType> relay(@NotNull ResourceLocation resloc, boolean throwIfNull) {
        if (throwIfNull && !RELAYS.inverse().containsKey(resloc)) {
            throw new IllegalArgumentException("Attempted to get a nonexistent definition for a resloc!");
        }
        return RELAYS.inverse().get(resloc);
    }

    public static @Nullable TransferRuleset newEndpointRuleset(AxonType type, Dist dist) {
        TransferRuleset pattern = ENDPOINT_RULES.get(type);
        return pattern == null ? null : pattern.createNew(dist);
    }
}
