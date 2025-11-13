package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.KnifeAction;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.common.item.KnifeItem;
import com.m_w_k.synapse.common.item.ModuleItem;
import com.m_w_k.synapse.common.item.SynapseTiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public final class SynapseItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SynapseMod.MODID);

    public static final List<RegistryObject<? extends Item>> SIMPLE = new ObjectArrayList<>();
    public static final List<RegistryObject<? extends Item>> PSEUDOSIMPLE = new ObjectArrayList<>();

    public static final RegistryObject<AxonItem> ITEM_AXON = pseudoSimpleModel("axon.item",
            () -> new AxonItem(p(), AxonType.ITEM));
    public static final RegistryObject<AxonItem> FLUID_AXON = pseudoSimpleModel("axon.fluid",
            () -> new AxonItem(p(), AxonType.FLUID));
    public static final RegistryObject<AxonItem> ENERGY_AXON = pseudoSimpleModel("axon.energy",
            () -> new AxonItem(p(), AxonType.ENERGY));
    public static final RegistryObject<AxonItem> REDSTONE_AXON = pseudoSimpleModel("axon.redstone",
            () -> new AxonItem(p(), AxonType.REDSTONE));
    public static final RegistryObject<ModuleItem> ITEM_MODULE = pseudoSimpleModel("module.item",
            () -> new ModuleItem(p(), AxonType.ITEM));
    public static final RegistryObject<ModuleItem> FLUID_MODULE = pseudoSimpleModel("module.fluid",
            () -> new ModuleItem(p(), AxonType.FLUID));
    public static final RegistryObject<ModuleItem> ENERGY_MODULE = pseudoSimpleModel("module.energy",
            () -> new ModuleItem(p(), AxonType.ENERGY));
    public static final RegistryObject<ModuleItem> REDSTONE_MODULE = pseudoSimpleModel("module.redstone",
            () -> new ModuleItem(p(), AxonType.REDSTONE));

    public static final RegistryObject<KnifeItem> BIOSTEEL_KNIFE = pseudoSimpleModel("biosteel_knife",
            () -> new KnifeItem(SynapseTiers.BIOSTEEL, 2, -1.8F, KnifeAction.SEVER, p()));
    public static final RegistryObject<KnifeItem> DUNED_GOLD_KNIFE = pseudoSimpleModel("duned_gold_knife",
            () -> new KnifeItem(SynapseTiers.DUNED_GOLD, 2, -1.8F, KnifeAction.REMOVE, p()));


    public static final RegistryObject<Item> BIOSTEEL = simpleModel("biosteel_ingot");
    public static final RegistryObject<Item> BIOSTEEL_NUGGET = simpleModel("biosteel_nugget");
    public static final RegistryObject<Item> DUNED_GOLD = simpleModel("duned_gold_ingot");
    public static final RegistryObject<Item> DUNED_GOLD_NUGGET = simpleModel("duned_gold_nugget");
    public static final RegistryObject<Item> NEURAL_THREAD = simpleModel("neural_thread");
    public static final RegistryObject<Item> TRANSFER_POWDER = simpleModel("transfer_powder");
    public static final RegistryObject<Item> WETWARE_CHIP = simpleModel("wetware_chip");

    public static final RegistryObject<Item> PIEZOELECTRIC_CRYSTAL = simpleModel("piezoelectric_crystal");
    public static final RegistryObject<Item> MAGMA_CRYSTAL = simpleModel("magma_crystal");
    public static final RegistryObject<Item> ENDER_CRYSTAL = simpleModel("ender_crystal");

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }
    
    private static Item.Properties p() {
        return new Item.Properties();
    }

    private static RegistryObject<Item> simpleModel(String name) {
        RegistryObject<Item> ret = ITEMS.register(name, () -> new Item(p()));
        SIMPLE.add(ret);
        return ret;
    }

    private static <T extends Item> RegistryObject<T> pseudoSimpleModel(String name, Supplier<T> sup) {
        RegistryObject<T> ret = ITEMS.register(name, sup);
        PSEUDOSIMPLE.add(ret);
        return ret;
    }
}
