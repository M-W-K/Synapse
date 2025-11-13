package com.m_w_k.synapse.data;

import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import com.m_w_k.synapse.registry.SynapseRecipeSerializerRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class SynapseRecipeProvider extends RecipeProvider {

    public SynapseRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        materialRecipes(writer);
        functionalRecipes(writer);
        decorationalRecipes(writer);
    }

    private void materialRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.BIOSTEEL.get())
                .requires(Tags.Items.INGOTS_IRON)
                .requires(Items.ROTTEN_FLESH, 2)
                .requires(Items.COAL)
                .unlockedBy("has_rotten_flesh", has(Items.ROTTEN_FLESH))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.DUNED_GOLD.get())
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(Tags.Items.BONES)
                .requires(Tags.Items.BONES)
                .requires(Tags.Items.SAND)
                .unlockedBy("has_bones", has(Tags.Items.BONES))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.NEURAL_THREAD.get())
                .requires(Tags.Items.GEMS_LAPIS)
                .requires(Tags.Items.STRING)
                .requires(Tags.Items.STRING)
                .requires(Items.GLOW_INK_SAC)
                .unlockedBy("has_string", has(Tags.Items.STRING))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.TRANSFER_POWDER.get())
                .requires(Tags.Items.DUSTS_REDSTONE)
                .requires(Items.GUNPOWDER, 2)
                .requires(Tags.Items.DUSTS_GLOWSTONE)
                .unlockedBy("has_gunpowder", has(Tags.Items.GUNPOWDER))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.WETWARE_CHIP.get())
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(Items.FERMENTED_SPIDER_EYE, 2)
                .requires(Tags.Items.GEMS_AMETHYST)
                .unlockedBy("has_spider_eyes", has(Items.SPIDER_EYE))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.PIEZOELECTRIC_CRYSTAL.get())
                .requires(Tags.Items.GEMS_QUARTZ)
                .requires(Tags.Items.GEMS_PRISMARINE)
                .requires(Tags.Items.GEMS_PRISMARINE)
                .requires(Items.PACKED_ICE)
                .unlockedBy("has_prismarine", has(Tags.Items.GEMS_PRISMARINE))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.MAGMA_CRYSTAL.get())
                .requires(Tags.Items.GEMS_EMERALD)
                .requires(Items.MAGMA_CREAM, 2)
                .requires(Tags.Items.INGOTS_NETHER_BRICK)
                .unlockedBy("has_magma_cream", has(Items.MAGMA_CREAM))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.ENDER_CRYSTAL.get())
                .requires(Tags.Items.GEMS_DIAMOND)
                .requires(Tags.Items.ENDER_PEARLS)
                .requires(Tags.Items.ENDER_PEARLS)
                .requires(Items.CHORUS_FRUIT)
                .unlockedBy("has_pearls", has(Tags.Items.ENDER_PEARLS))
                .save(writer);
    }

    private void functionalRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        knife(writer, SynapseItemRegistry.BIOSTEEL_KNIFE.get(), SynapseItemRegistry.BIOSTEEL.get(), SynapseItemRegistry.BIOSTEEL_NUGGET.get());
        knife(writer, SynapseItemRegistry.DUNED_GOLD_KNIFE.get(), SynapseItemRegistry.DUNED_GOLD.get(), SynapseItemRegistry.DUNED_GOLD_NUGGET.get());

        axonType(writer, Items.GOLD_INGOT, SynapseItemRegistry.ENERGY_AXON.get(), SynapseItemRegistry.ENERGY_MODULE.get());
        axonType(writer, Items.IRON_INGOT, SynapseItemRegistry.ITEM_AXON.get(), SynapseItemRegistry.ITEM_MODULE.get());
        axonType(writer, Items.COPPER_INGOT, SynapseItemRegistry.FLUID_AXON.get(), SynapseItemRegistry.FLUID_MODULE.get());
        axonType(writer, Items.REDSTONE, SynapseItemRegistry.REDSTONE_AXON.get(), SynapseItemRegistry.REDSTONE_MODULE.get());

        SpecialRecipeBuilder.special(SynapseRecipeSerializerRegistry.ADD_MODULE.get()).save(writer, "add_module");
        SpecialRecipeBuilder.special(SynapseRecipeSerializerRegistry.REMOVE_MODULE.get()).save(writer, "remove_module");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.ENDPOINT_BASIC.get(), 2)
                .pattern("btb")
                .pattern(" c ")
                .pattern("btb")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.TRANSFER_POWDER.get())
                .unlockedBy("has_biosteel", has(SynapseItemRegistry.BIOSTEEL.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.RELAY.get(), 2)
                .pattern("b b")
                .pattern("tct")
                .pattern("b b")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.NEURAL_THREAD.get())
                .unlockedBy("has_biosteel", has(SynapseItemRegistry.BIOSTEEL.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get())
                .pattern("gbg")
                .pattern("tct")
                .pattern("gbg")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('g', SynapseItemRegistry.DUNED_GOLD.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.PIEZOELECTRIC_CRYSTAL.get())
                .unlockedBy("has_duned_gold", has(SynapseItemRegistry.DUNED_GOLD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get())
                .pattern("gbg")
                .pattern("tct")
                .pattern("gbg")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('g', SynapseItemRegistry.DUNED_GOLD.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.MAGMA_CRYSTAL.get())
                .unlockedBy("has_duned_gold", has(SynapseItemRegistry.DUNED_GOLD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get())
                .pattern("gbg")
                .pattern("tct")
                .pattern("gbg")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('g', SynapseItemRegistry.DUNED_GOLD.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.ENDER_CRYSTAL.get())
                .unlockedBy("has_duned_gold", has(SynapseItemRegistry.DUNED_GOLD.get()))
                .save(writer);
    }

    private void decorationalRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        nineBlockStorageRecipes(writer, RecipeCategory.MISC, SynapseItemRegistry.BIOSTEEL_NUGGET.get(),
                RecipeCategory.MISC, SynapseItemRegistry.BIOSTEEL.get(), "synapse:biosteel_ingot_from_nuggets", "synapse:biosteel_ingot", "synapse:biosteel_nugget", null);
        nineBlockStorageRecipes(writer, RecipeCategory.MISC, SynapseItemRegistry.DUNED_GOLD_NUGGET.get(),
                RecipeCategory.MISC, SynapseItemRegistry.DUNED_GOLD.get(), "synapse:duned_gold_ingot_from_nuggets", "synapse:duned_gold_ingot", "synapse:duned_gold_nugget", null);
    }

    private void axonType(@NotNull Consumer<FinishedRecipe> writer, ItemLike mat, ItemLike axon, ItemLike module) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, axon, 3)
                .pattern("sis")
                .pattern("iti")
                .pattern("sis")
                .define('i', mat)
                .define('s', SynapseItemRegistry.NEURAL_THREAD.get())
                .define('t', SynapseItemRegistry.TRANSFER_POWDER.get())
                .unlockedBy("has_thread", has(SynapseItemRegistry.NEURAL_THREAD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, module, 2)
                .pattern(" n ")
                .pattern("nin")
                .pattern(" n ")
                .define('i', mat)
                .define('n', SynapseItemRegistry.BIOSTEEL_NUGGET.get())
                .unlockedBy("has_biosteel", has(SynapseItemRegistry.BIOSTEEL.get()))
                .save(writer);
    }

    private void knife(@NotNull Consumer<FinishedRecipe> writer, ItemLike knife, ItemLike ingot, ItemLike nugget) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, knife)
                .pattern("  n")
                .pattern("ni ")
                .pattern("sn ")
                .define('s', Items.STICK)
                .define('i', ingot)
                .define('n', nugget)
                .unlockedBy("has_ingot", has(ingot))
                .save(writer);
    }
}
