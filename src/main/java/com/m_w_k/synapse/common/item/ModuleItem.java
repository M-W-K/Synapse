package com.m_w_k.synapse.common.item;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.item.AxonTypeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModuleItem extends Item implements AxonTypeItem {
    protected final @NotNull AxonType type;

    public ModuleItem(Properties p_41383_, @NotNull AxonType type) {
        super(p_41383_);
        this.type = type;
    }

    @Override
    public @NotNull AxonType getType() {
        return type;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("item.synapse.module.desc").withStyle(ChatFormatting.GRAY));
    }
}
