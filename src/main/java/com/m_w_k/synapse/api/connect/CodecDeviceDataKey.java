package com.m_w_k.synapse.api.connect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class CodecDeviceDataKey<T> extends DeviceDataKey<T> {

    private final Codec<T> codec;
    private final Function<DataResult.PartialResult<T>, T> afterParseFailure;

    public CodecDeviceDataKey(ResourceLocation loc, Codec<T> codec) {
        this(loc, codec, r -> null);
    }

    public CodecDeviceDataKey(ResourceLocation loc, Codec<T> codec, Function<DataResult.PartialResult<T>, T> afterParseFailure) {
        super(loc);
        this.codec = codec;
        this.afterParseFailure = afterParseFailure;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull T t) {
        CompoundTag tag;
        Optional<Tag> opt = codec.encodeStart(NbtOps.INSTANCE, t).get().left();
        if (opt.isPresent()) {
            if (opt.get() instanceof CompoundTag c) {
                tag = c;
            } else {
                tag = new CompoundTag();
                tag.put("CodecSubtag", opt.get());
            }
        } else {
            return new CompoundTag();
        }
        return tag;
    }

    @Override
    public @NotNull T load(@NotNull CompoundTag tag) {
        Tag t;
        if (tag.contains("CodecSubtag")) {
            t = tag.get("CodecSubtag");
        } else {
            t = tag;
        }
        var result = codec.parse(NbtOps.INSTANCE, t).get();
        return result.map(UnaryOperator.identity(), afterParseFailure);
    }
}
