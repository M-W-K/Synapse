package com.m_w_k.synapse.data;

import com.m_w_k.synapse.api.block.ModuleDataProtocols;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.common.block.RelayBlock;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SynapseLootTableGen implements DataProvider.Factory<LootTableProvider> {

    @Override
    public @NotNull LootTableProvider create(@NotNull PackOutput output) {
        return new LootTableProvider(output, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(BlockSubProvider::new, LootContextParamSets.BLOCK)
        ));
    }

    private static final class BlockSubProvider extends BlockLootSubProvider {

        public BlockSubProvider() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return SynapseBlockRegistry.BLOCKS.getEntries()
                    .stream()
                    .flatMap(RegistryObject::stream)
                    ::iterator;
        }

        @Override
        protected void generate() {
            this.add(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get(), (block) -> LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(this.applyExplosionDecay(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get(),
                                    LootItem.lootTableItem(block)
                                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(ModuleDataProtocols.BE_KEY, ModuleDataProtocols.STACK_KEY))))
            ));
            this.add(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get(), (block) -> LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(this.applyExplosionDecay(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get(),
                                    LootItem.lootTableItem(block)
                                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(ModuleDataProtocols.BE_KEY, ModuleDataProtocols.STACK_KEY))))
            ));
            this.add(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get(), (block) -> LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(this.applyExplosionDecay(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get(),
                                    LootItem.lootTableItem(block)
                                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(ModuleDataProtocols.BE_KEY, ModuleDataProtocols.STACK_KEY))))
            ));
//            this.dropSelf(SynapseBlockRegistry.DISTRIBUTOR_ALIAS_SYSTEM_SERVER.get());

            this.add(SynapseBlockRegistry.ENDPOINT_BASIC.get(), block -> {
                var builder = LootTable.lootTable();
                for (Direction d : Direction.values()) {
                    builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(this.applyExplosionDecay(SynapseBlockRegistry.ENDPOINT_BASIC.get(), LootItem.lootTableItem(block))
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(EndpointBlock.PROPERTY_BY_DIRECTION.get(d), true)))
                                    .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(ModuleDataProtocols.fullEndpointBEKey(d), ModuleDataProtocols.STACK_KEY))));
                }
                return builder;
            });
            this.add(SynapseBlockRegistry.RELAY.get(), (block) -> {
                var builder = LootTable.lootTable();
                for (int i = 0; i < 4; i++) {
                    builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(this.applyExplosionDecay(SynapseBlockRegistry.RELAY.get(), LootItem.lootTableItem(block))
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(RelayBlock.PROPERTY_BY_INT[i], true)))
                                    .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(ModuleDataProtocols.fullRelayBEKey(i), ModuleDataProtocols.STACK_KEY))));
                }
                return builder;
            });
        }
    }
}
