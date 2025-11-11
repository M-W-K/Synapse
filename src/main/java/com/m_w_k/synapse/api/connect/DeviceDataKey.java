package com.m_w_k.synapse.api.connect;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class DeviceDataKey<T> {
    public static final Map<ResourceLocation, DeviceDataKey<?>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static final Codec<Map<DeviceDataKey<?>, Object>> MAP_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, CompoundTag.CODEC).xmap(m -> {
                Map<DeviceDataKey<?>, Object> map = new Object2ObjectOpenHashMap<>(m.size());
                m.forEach((loc, tag) -> {
                    var k = REGISTRY.get(loc);
                    if (k == null) return;
                    map.put(k, k.load(tag));
                });
                return map;
            }, map -> {
                Map<ResourceLocation, CompoundTag> m = new Object2ObjectOpenHashMap<>(map.size());
                map.forEach((k, v) -> m.put(k.loc(), k.saveO(k.cast(v))));
                return m;
            });

    private final ResourceLocation loc;

    public DeviceDataKey(ResourceLocation loc) {
        this.loc = loc;
        if (REGISTRY.containsKey(loc))
            throw new IllegalStateException("Duplicate registration of device data keys detected!");
        REGISTRY.put(loc, this);
    }

    public ResourceLocation loc() {
        return loc;
    }

    @Contract("_,!null->!null")
    public @Nullable T getFromMap(@NotNull Map<DeviceDataKey<?>, Object> map, @Nullable T fallback) {
        return cast(map.getOrDefault(this, fallback));
    }

    public T computeIfAbsent(@NotNull Map<DeviceDataKey<?>, Object> map, @NotNull Supplier<T> compute) {
        return cast(map.computeIfAbsent(this, k -> compute.get()));
    }

    public T cast(Object o) {
        return (T) o;
    }

    @NotNull
    public CompoundTag saveO(@NotNull Object o) {
        return save(cast(o));
    }

    @NotNull
    public abstract CompoundTag save(@NotNull T t);

    @NotNull
    public abstract T load(@NotNull CompoundTag tag);

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DeviceDataKey) obj;
        return Objects.equals(this.loc, that.loc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loc);
    }

    @Override
    public String toString() {
        return "DeviceDataKey[" +
                "loc=" + loc + ']';
    }

}
