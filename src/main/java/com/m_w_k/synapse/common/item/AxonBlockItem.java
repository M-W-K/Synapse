package com.m_w_k.synapse.common.item;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.ModuleDataProtocols;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.block.AxonBlock;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class AxonBlockItem extends BlockItem {

    private static final Codec<Map<AxonType, ItemStack>> MODULE_CODEC = Codec.unboundedMap(AxonType.CODEC, ItemStack.CODEC);

    public AxonBlockItem(AxonBlock p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public @NotNull AxonBlock getBlock() {
        return (AxonBlock) super.getBlock();
    }

    public @NotNull @UnmodifiableView Map<AxonType, ItemStack> installedModules(@NotNull ItemStack stack) {
        return readModules(stack.getTagElement(ModuleDataProtocols.STACK_KEY));
    }

    public static @NotNull @UnmodifiableView Map<AxonType, ItemStack> readModules(@Nullable CompoundTag tag) {
        if (tag == null) return Collections.emptyMap();
        return MODULE_CODEC.parse(NbtOps.INSTANCE, tag).get().map(UnaryOperator.identity(), e -> Collections.emptyMap());
    }

    protected void writeModules(@NotNull ItemStack stack, Map<AxonType, ItemStack> map) {
        writeModules(map).ifPresent( tag -> {
            if (tag.isEmpty()) stack.removeTagKey(ModuleDataProtocols.STACK_KEY);
            else stack.addTagElement(ModuleDataProtocols.STACK_KEY, tag);
        });
    }

    public static Optional<CompoundTag> writeModules(@NotNull Map<AxonType, ItemStack> map) {
        return MODULE_CODEC.encodeStart(NbtOps.INSTANCE, map).get().left().map(t -> {
            if (t instanceof CompoundTag c) {
                return c;
            }
            return null;
        });
    }

    public boolean installModule(@NotNull ItemStack stack, @NotNull ModuleItem module, @NotNull ItemStack moduleStack) {
        var map = installedModules(stack);
        if (map.containsKey(module.getType())) {
            return false;
        }
        map = new Object2ObjectRBTreeMap<>(map);
        map.put(module.getType(), moduleStack.copyWithCount(1));
        writeModules(stack, map);
        return true;
    }

    public @Nullable ItemStack popModule(@NotNull ItemStack stack) {
        var map = installedModules(stack);
        if (map.isEmpty()) return null;
        map = new Object2ObjectRBTreeMap<>(map);
        ItemStack pop = map.remove(((Object2ObjectRBTreeMap<AxonType, ItemStack>) map).firstKey());
        writeModules(stack, map);
        return pop;
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext blockPlaceContext, @NotNull BlockState state) {
        if (super.placeBlock(blockPlaceContext, state)) {
            BlockPos pos = blockPlaceContext.getClickedPos();
            Level level = blockPlaceContext.getLevel();
            if (level.isClientSide()) return true;
            BlockState state1 = level.getBlockState(pos);
            if (state1.is(state.getBlock()) && level.getBlockEntity(pos) instanceof IAxonBlockEntity abe) {
                abe.installModules(this, blockPlaceContext);
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, components, flag);
        var map = installedModules(stack);
        if (!map.isEmpty()) {
            components.add(Component.translatable("item.synapse.module.installed.1").withStyle(ChatFormatting.GRAY));
            for (AxonType type : map.keySet()) {
                components.add(Component.translatable("item.synapse.module.installed.2",
                        type.getSerializedName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
