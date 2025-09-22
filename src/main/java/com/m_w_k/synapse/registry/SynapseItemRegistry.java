package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public abstract class SynapseItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SynapseMod.MODID);

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }
}
