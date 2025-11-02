package com.m_w_k.synapse.api.block.ruleset;

import net.minecraftforge.fluids.FluidStack;

public interface FluidRuleAccess extends RuleAccess {

    FluidStack getMatchStack(int index);

    void setMatchStack(int index, FluidStack stack);

    boolean isWhitelist();

    void setWhitelist(boolean whitelist);

    boolean isMatchNBT();

    void setMatchNBT(boolean matchNBT);
}
