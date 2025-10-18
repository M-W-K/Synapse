package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public interface FluidRuleAccess extends RuleAccess {

    FluidStack getMatchStack(int index);

    void setMatchStack(int index, FluidStack stack);

    boolean isWhitelist();

    void setWhitelist(boolean whitelist);

    boolean isMatchNBT();

    void setMatchNBT(boolean matchNBT);
}
