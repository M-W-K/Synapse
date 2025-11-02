package com.m_w_k.synapse.api.block.ruleset;

import net.minecraft.world.item.ItemStack;

public interface ItemRuleAccess extends RuleAccess {

    ItemStack getMatchStack(int index);

    void setMatchStack(int index, ItemStack stack);

    boolean isWhitelist();

    void setWhitelist(boolean whitelist);

    boolean isMatchNBT();

    void setMatchNBT(boolean matchNBT);
}
