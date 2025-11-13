package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.item.crafting.AddModuleRecipe;
import com.m_w_k.synapse.common.item.crafting.RemoveModuleRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class SynapseRecipeSerializerRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, SynapseMod.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<AddModuleRecipe>> ADD_MODULE = SERIALIZERS.register("addmodule",
            () -> new SimpleCraftingRecipeSerializer<>(AddModuleRecipe::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<RemoveModuleRecipe>> REMOVE_MODULE = SERIALIZERS.register("removemodule",
            () -> new SimpleCraftingRecipeSerializer<>(RemoveModuleRecipe::new));

    public static void init(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

}
