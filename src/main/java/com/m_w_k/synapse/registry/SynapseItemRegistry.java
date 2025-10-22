package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.item.AxonItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class SynapseItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SynapseMod.MODID);

    public static final List<RegistryObject<Item>> SIMPLE = new ObjectArrayList<>();

    public static final RegistryObject<AxonItem> ENERGY_AXON = ITEMS.register("axon.energy",
            () -> new AxonItem(new Item.Properties(), AxonType.ENERGY));
    public static final RegistryObject<AxonItem> ITEM_AXON = ITEMS.register("axon.item",
            () -> new AxonItem(new Item.Properties(), AxonType.ITEM));
    public static final RegistryObject<AxonItem> FLUID_AXON = ITEMS.register("axon.fluid",
            () -> new AxonItem(new Item.Properties(), AxonType.FLUID));


    public static final RegistryObject<Item> BIOSTEEL = simple("biosteel_ingot");
    public static final RegistryObject<Item> BIOSTEEL_NUGGET = simple("biosteel_nugget");
    public static final RegistryObject<Item> DUNED_GOLD = simple("duned_gold_ingot");
    public static final RegistryObject<Item> DUNED_GOLD_NUGGET = simple("duned_gold_nugget");
    public static final RegistryObject<Item> NEURAL_THREAD = simple("neural_thread");
    public static final RegistryObject<Item> TRANSFER_POWDER = simple("transfer_powder");
    public static final RegistryObject<Item> WETWARE_CHIP = simple("wetware_chip");

    public static final RegistryObject<Item> PIEZOELECTRIC_CRYSTAL = simple("piezoelectric_crystal");
    public static final RegistryObject<Item> MAGMA_CRYSTAL = simple("magma_crystal");
    public static final RegistryObject<Item> ENDER_CRYSTAL = simple("ender_crystal");

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    private static RegistryObject<Item> simple(String name) {
        RegistryObject<Item> ret = ITEMS.register(name, () -> new Item(new Item.Properties()));
        SIMPLE.add(ret);
        return ret;
    }
}
