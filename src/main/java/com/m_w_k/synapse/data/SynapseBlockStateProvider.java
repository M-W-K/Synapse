package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.common.block.RelayBlock;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
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
        ModelFile endpoint = models().getExistingFile(modLoc("block/endpoint_base"));
        getMultipartBuilder(SynapseBlockRegistry.ENDPOINT_BASIC.get())
                .part().modelFile(endpoint).addModel().condition(EndpointBlock.NORTH, true).end()
                .part().modelFile(endpoint).rotationY(90).addModel().condition(EndpointBlock.EAST, true).end()
                .part().modelFile(endpoint).rotationY(180).addModel().condition(EndpointBlock.SOUTH, true).end()
                .part().modelFile(endpoint).rotationY(270).addModel().condition(EndpointBlock.WEST, true).end()
                .part().modelFile(endpoint).rotationX(90).addModel().condition(EndpointBlock.DOWN, true).end()
                .part().modelFile(endpoint).rotationX(-90).addModel().condition(EndpointBlock.UP, true).end();

        simpleBlockItem(SynapseBlockRegistry.ENDPOINT_BASIC.get(), endpoint);
        var builder = getMultipartBuilder(SynapseBlockRegistry.RELAY.get());
        for (Direction dir : Direction.values()) {
            relaySector(builder, dir);
        }
        simpleBlockItem(SynapseBlockRegistry.RELAY.get(), models().getExistingFile(modLoc("block/relay_1")));
    }

    private void relaySector(MultiPartBlockStateBuilder relayBuilder, Direction sector) {
        int x = 0;
        if (sector.getAxis().isHorizontal()) {
            x = -90;
        } else if (sector == Direction.DOWN) {
            x = 180;
        }
        relayBuilder.part().modelFile(models().getExistingFile(modLoc("block/relay_1")))
                .rotationX(x).rotationY((int) sector.toYRot()).addModel()
                .condition(RelayBlock.MOUNT_DIRECTION, sector).condition(RelayBlock.RELAYS, 1, 2, 3, 4).end();
        relayBuilder.part().modelFile(models().getExistingFile(modLoc("block/relay_2")))
                .rotationX(x).rotationY((int) sector.toYRot()).addModel()
                .condition(RelayBlock.MOUNT_DIRECTION, sector).condition(RelayBlock.RELAYS, 2, 3, 4).end();
        relayBuilder.part().modelFile(models().getExistingFile(modLoc("block/relay_3")))
                .rotationX(x).rotationY((int) sector.toYRot()).addModel()
                .condition(RelayBlock.MOUNT_DIRECTION, sector).condition(RelayBlock.RELAYS, 3, 4).end();
        relayBuilder.part().modelFile(models().getExistingFile(modLoc("block/relay_4")))
                .rotationX(x).rotationY((int) sector.toYRot()).addModel()
                .condition(RelayBlock.MOUNT_DIRECTION, sector).condition(RelayBlock.RELAYS, 4).end();
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
