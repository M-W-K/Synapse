package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SynapseBlockTagsProvider extends BlockTagsProvider {
    public SynapseBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SynapseMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider prov) {
        tag(TagKey.create(Registries.BLOCK, new ResourceLocation("mineable/pickaxe")))
                .add(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get())
                .add(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get())
                .add(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get())
                .add(SynapseBlockRegistry.ENDPOINT_BASIC.get())
                .add(SynapseBlockRegistry.RELAY.get());
    }
}
