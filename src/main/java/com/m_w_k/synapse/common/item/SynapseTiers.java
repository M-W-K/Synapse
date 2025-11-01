package com.m_w_k.synapse.common.item;

import com.m_w_k.synapse.registry.SynapseItemRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;

public final class SynapseTiers {

    public static final ForgeTier BIOSTEEL = new ForgeTier(2, 750, 6.0F, 2.0F, 22,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(SynapseItemRegistry.BIOSTEEL.get()));
    public static final ForgeTier DUNED_GOLD = new ForgeTier(0, 160, 12.0F, 0.0F, 28,
            Tags.Blocks.NEEDS_GOLD_TOOL, () -> Ingredient.of(SynapseItemRegistry.DUNED_GOLD.get()));
}
