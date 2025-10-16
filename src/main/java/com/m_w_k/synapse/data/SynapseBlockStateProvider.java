package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SynapseBlockStateProvider extends BlockStateProvider {
    public SynapseBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SynapseMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simple(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get(), "block/distributor/tier_1");
        simple(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get(), "block/distributor/tier_2");
        simple(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get(), "block/distributor/tier_3");
    }

    private void simple(Block block, String file) {
        ModelFile model = models().getExistingFile(modLoc(file));
        this.getVariantBuilder(block)
                .forAllStates(state ->
                        ConfiguredModel.builder()
                                .modelFile(model)
                                .build()
                );
        simpleBlockItem(block, model);
    }
}
