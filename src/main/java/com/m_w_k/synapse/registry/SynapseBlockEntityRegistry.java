package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.connect.ConnectionTier;
import com.m_w_k.synapse.block.DistributorBlock;
import com.m_w_k.synapse.block.entity.DistributorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public abstract class SynapseBlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SynapseMod.MODID);

    public static final RegistryObject<BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR_BLOCK = BLOCK_ENTITY_TYPES.register("distributor_block_entity",
            () -> BlockEntityType.Builder.of((pos, state) -> {
                    if (state.getBlock() instanceof DistributorBlock d) {
                        return d.newBlockEntity(pos, state);
                    }
                    return null;
                },
                    SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get(), SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get(),
                    SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get()).build(null));

    public static void init(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
