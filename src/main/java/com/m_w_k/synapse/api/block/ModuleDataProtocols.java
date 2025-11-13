package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.DeviceDataKeys;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class ModuleDataProtocols {

    public static final String BE_KEY = "ModuleInfo";
    public static final String STACK_KEY = BE_KEY;

    public static String fullEndpointBEKey(@NotNull Direction dir) {
        return BE_KEY + "." + endpointBEKey(dir);
    }

    public static String endpointBEKey(@NotNull Direction dir) {
        return dir.name();
    }

    public static String fullRelayBEKey(@Range(from = 0, to = 3) int index) {
        return BE_KEY + "." + relayBEKey(index);
    }

    public static String relayBEKey(@Range(from = 0, to = 3) int index) {
        return Integer.toString(index);
    }

    public static void readStandardModules(@NotNull Function<ResourceLocation, LocalConnectorDevice> getOrCreateDevice,
                                           @NotNull ItemStack stack) {
        AxonBlockItem.readModules(stack.getTagElement(STACK_KEY)).forEach((t, s) -> {
            ResourceLocation def = AxonDeviceDefinitions.standard(t, false);
            if (def != null) {
                getOrCreateDevice.apply(def).getData().put(DeviceDataKeys.MODULE_STACK, s);
            }
        });
    }

    public static @NotNull Optional<CompoundTag> writeStandardModules(@NotNull Map<ResourceLocation, LocalConnectorDevice> devices) {
        Map<AxonType, ItemStack> pass = new Object2ObjectOpenHashMap<>();
        devices.forEach((r, s) -> {
            AxonType def = AxonDeviceDefinitions.standard(r, false);
            if (def != null) {
                ItemStack put = s.getData(DeviceDataKeys.MODULE_STACK, null);
                if (put == null) {
                    // backwards compatibility
                    put = switch (def) {
                        case ITEM -> new ItemStack(SynapseItemRegistry.ITEM_MODULE.get());
                        case FLUID -> new ItemStack(SynapseItemRegistry.FLUID_MODULE.get());
                        case ENERGY -> new ItemStack(SynapseItemRegistry.ENERGY_MODULE.get());
                        default -> ItemStack.EMPTY;
                    };
                }
                pass.put(def, put);
            }
        });
        return AxonBlockItem.writeModules(pass);
    }

    public static void readEndpointModules(@NotNull Function<ResourceLocation, LocalConnectorDevice> getOrCreateDevice,
                                           @NotNull ItemStack stack, @NotNull Direction dir) {
        AxonBlockItem.readModules(stack.getTagElement(STACK_KEY)).forEach((t, s) -> {
            ResourceLocation def = AxonDeviceDefinitions.endpoint(t, dir, false);
            if (def != null) {
                getOrCreateDevice.apply(def).getData().put(DeviceDataKeys.MODULE_STACK, s);
            }
        });
    }

    public static @NotNull Optional<CompoundTag> writeEndpointModules(@NotNull Map<ResourceLocation, LocalConnectorDevice> devices,
                                                                      @NotNull Direction dir) {
        Map<AxonType, ItemStack> pass = new Object2ObjectOpenHashMap<>();
        devices.forEach((r, s) -> {
            Pair<AxonType, Direction> def = AxonDeviceDefinitions.endpoint(r, false);
            if (def != null && def.value() == dir) {
                ItemStack put = s.getData(DeviceDataKeys.MODULE_STACK, null);
                if (put == null) {
                    // backwards compatibility
                    put = switch (def.key()) {
                        case ITEM -> new ItemStack(SynapseItemRegistry.ITEM_MODULE.get());
                        case FLUID -> new ItemStack(SynapseItemRegistry.FLUID_MODULE.get());
                        case ENERGY -> new ItemStack(SynapseItemRegistry.ENERGY_MODULE.get());
                        default -> ItemStack.EMPTY;
                    };
                }
                pass.put(def.key(), put);
            }
        });
        return AxonBlockItem.writeModules(pass);
    }

    public static void readRelayModules(@NotNull Function<ResourceLocation, LocalConnectorDevice> getOrCreateDevice,
                                           @NotNull ItemStack stack, @Range(from = 0, to = 3) int index) {
        AxonBlockItem.readModules(stack.getTagElement(STACK_KEY)).forEach((t, s) -> {
            ResourceLocation def = AxonDeviceDefinitions.relay(t, index, false);
            if (def != null) {
                getOrCreateDevice.apply(def).getData().put(DeviceDataKeys.MODULE_STACK, s);
            }
        });
    }

    public static @NotNull Optional<CompoundTag> writeRelayModules(@NotNull Map<ResourceLocation, LocalConnectorDevice> devices,
                                                                      @Range(from = 0, to = 3) int index) {
        Map<AxonType, ItemStack> pass = new Object2ObjectOpenHashMap<>();
        devices.forEach((r, s) -> {
            IntObjectPair<AxonType> def = AxonDeviceDefinitions.relay(r, false);
            if (def != null && def.keyInt() == index) {
                ItemStack put = s.getData(DeviceDataKeys.MODULE_STACK, null);
                if (put == null) {
                    // backwards compatibility
                    put = switch (def.second()) {
                        case ITEM -> new ItemStack(SynapseItemRegistry.ITEM_MODULE.get());
                        case FLUID -> new ItemStack(SynapseItemRegistry.FLUID_MODULE.get());
                        case ENERGY -> new ItemStack(SynapseItemRegistry.ENERGY_MODULE.get());
                        default -> ItemStack.EMPTY;
                    };
                }
                pass.put(def.value(), put);
            }
        });
        return AxonBlockItem.writeModules(pass);
    }
}
