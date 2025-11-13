package com.m_w_k.synapse.common.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.common.item.ModuleItem;
import com.m_w_k.synapse.registry.SynapseRecipeSerializerRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddModuleRecipe extends CustomRecipe {
    public AddModuleRecipe(ResourceLocation p_250543_, CraftingBookCategory p_248679_) {
        super(p_250543_, p_248679_);
    }

    public boolean matches(CraftingContainer p_44138_, Level p_44139_) {
        ItemStack axonBlockItem = null;
        Set<AxonType> modules = new ReferenceOpenHashSet<>();
        for(int i = 0; i < p_44138_.getContainerSize(); ++i) {
            ItemStack itemstack = p_44138_.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof AxonBlockItem) {
                    if (axonBlockItem != null) return false;
                    axonBlockItem = itemstack;
                } else if (itemstack.getItem() instanceof ModuleItem m) {
                    modules.add(m.getType());
                } else {
                    return false;
                }
            }
        }
        if (axonBlockItem == null || modules.isEmpty()) return false;
        return ((AxonBlockItem) axonBlockItem.getItem()).getBlock()
                .allowModuleInstallItem(modules, ((AxonBlockItem) axonBlockItem.getItem()), axonBlockItem);
    }

    public ItemStack assemble(CraftingContainer p_44136_, @NotNull RegistryAccess p_267094_) {
        ItemStack axonBlockItem = null;
        List<ItemStack> foundModules = new ObjectArrayList<>();
        for(int i = 0; i < p_44136_.getContainerSize(); ++i) {
            ItemStack itemstack = p_44136_.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof AxonBlockItem) {
                    if (axonBlockItem != null) return ItemStack.EMPTY;
                    axonBlockItem = itemstack;
                } else if (itemstack.getItem() instanceof ModuleItem) {
                    foundModules.add(itemstack);
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (axonBlockItem == null || foundModules.isEmpty()) return ItemStack.EMPTY;

        ItemStack ret = axonBlockItem.copyWithCount(1);
        for (ItemStack stack : foundModules) {
            ((AxonBlockItem) axonBlockItem.getItem()).installModule(ret, (ModuleItem) stack.getItem(), stack);
        }
        return ret;
    }

    public boolean canCraftInDimensions(int p_44128_, int p_44129_) {
        return p_44128_ * p_44129_ >= 2;
    }

    public @NotNull RecipeSerializer<?> getSerializer() {
        return SynapseRecipeSerializerRegistry.ADD_MODULE.get();
    }
}
