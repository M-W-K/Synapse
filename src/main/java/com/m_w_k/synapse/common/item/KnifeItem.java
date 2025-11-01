package com.m_w_k.synapse.common.item;

import com.m_w_k.synapse.api.KnifeAction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KnifeItem extends SwordItem {
    protected @NotNull KnifeAction action;

    public KnifeItem(Tier p_43269_, int p_43270_, float p_43271_, @NotNull KnifeAction action, Properties p_43272_) {
        super(p_43269_, p_43270_, p_43271_, p_43272_);
        this.action = action;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack p_43288_, @NotNull BlockState p_43289_) {
        return (float) (super.getDestroySpeed(p_43288_, p_43289_) * Math.sqrt(getTier().getSpeed()));
    }

    public @NotNull KnifeAction getAction() {
        return action;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        switch (action) {
            case SEVER -> list.add(Component.translatable("item.synapse.severing_knife").withStyle(ChatFormatting.GRAY));
            case REMOVE -> list.add(Component.translatable("item.synapse.removing_knife").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.SWORD_DIG ||
                (toolAction != ToolActions.SHEARS_DIG && ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
