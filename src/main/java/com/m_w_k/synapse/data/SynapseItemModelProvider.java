package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class SynapseItemModelProvider extends ItemModelProvider {
    public SynapseItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SynapseMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        SynapseItemRegistry.SIMPLE.forEach(o -> basicItem(o.get()));
        SynapseItemRegistry.PSEUDOSIMPLE.forEach(o -> pseudoBasicItem(o.get()));
    }

    public ItemModelBuilder pseudoBasicItem(Item item) {
        return pseudoBasicItem(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    }

    public ItemModelBuilder pseudoBasicItem(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", new ResourceLocation(item.getNamespace(), "item/" + item.getPath().replaceAll("\\.", "/")));
    }
}
