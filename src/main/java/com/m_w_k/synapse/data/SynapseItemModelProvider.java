package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SynapseItemModelProvider extends ItemModelProvider {
    public SynapseItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SynapseMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        SynapseItemRegistry.SIMPLE.forEach(o -> basicItem(o.get()));
        basicItem(SynapseItemRegistry.BIOSTEEL_KNIFE.get());
        basicItem(SynapseItemRegistry.DUNED_GOLD_KNIFE.get());
    }
}
