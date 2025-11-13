package com.m_w_k.synapse.common.item.crafting;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.KnifeAction;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.common.item.KnifeItem;
import com.m_w_k.synapse.common.item.ModuleItem;
import com.m_w_k.synapse.registry.SynapseRecipeSerializerRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveModuleRecipe extends CustomRecipe {
    public RemoveModuleRecipe(ResourceLocation p_250543_, CraftingBookCategory p_248679_) {
        super(p_250543_, p_248679_);
    }

    public boolean matches(CraftingContainer p_44138_, Level p_44139_) {
        boolean foundAxonBlockItem = false;
        boolean foundKnife = false;
        for(int i = 0; i < p_44138_.getContainerSize(); ++i) {
            ItemStack itemstack = p_44138_.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof AxonBlockItem) {
                    if (foundAxonBlockItem) return false;
                    foundAxonBlockItem = true;
                } else if (itemstack.getItem() instanceof KnifeItem) {
                    if (foundKnife) return false;
                    foundKnife = true;
                } else {
                    return false;
                }
            }
        }

        return foundAxonBlockItem && foundKnife;
    }

    public @NotNull ItemStack assemble(CraftingContainer p_44136_, @NotNull RegistryAccess p_267094_) {
        ItemStack axonBlockItem = null;
        KnifeItem knifeItem = null;
        for(int i = 0; i < p_44136_.getContainerSize(); ++i) {
            ItemStack itemstack = p_44136_.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof AxonBlockItem) {
                    if (axonBlockItem != null) return ItemStack.EMPTY;
                    axonBlockItem = itemstack;
                } else if (itemstack.getItem() instanceof KnifeItem knife) {
                    if (knifeItem != null) return ItemStack.EMPTY;
                    knifeItem = knife;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (axonBlockItem == null || knifeItem == null) return ItemStack.EMPTY;

        ItemStack ret = axonBlockItem.copyWithCount(1);
        if (knifeItem.getAction() == KnifeAction.SEVER) {
            if (((AxonBlockItem) axonBlockItem.getItem()).popModule(ret) == null) return ItemStack.EMPTY;
        } else if (knifeItem.getAction() == KnifeAction.REMOVE) {
            while (true) {
                if (((AxonBlockItem) axonBlockItem.getItem()).popModule(ret) == null) break;
            }
        }
        return ret;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingContainer p_44004_) {
        int biIndex = -1;
        ItemStack axonBlockItem = null;
        KnifeItem knifeItem = null;
        for(int i = 0; i < p_44004_.getContainerSize(); ++i) {
            ItemStack itemstack = p_44004_.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof AxonBlockItem) {
                    if (axonBlockItem != null) return super.getRemainingItems(p_44004_);
                    biIndex = i;
                    axonBlockItem = itemstack;
                } else if (itemstack.getItem() instanceof KnifeItem knife) {
                    if (knifeItem != null) return super.getRemainingItems(p_44004_);
                    knifeItem = knife;
                } else {
                    return super.getRemainingItems(p_44004_);
                }
            }
        }
        if (axonBlockItem == null || knifeItem == null) return super.getRemainingItems(p_44004_);

        ItemStack out = axonBlockItem.copyWithCount(1);
        if (knifeItem.getAction() == KnifeAction.SEVER) {
            ItemStack ret = ((AxonBlockItem) axonBlockItem.getItem()).popModule(out);
            if (ret == null) return super.getRemainingItems(p_44004_);
            NonNullList<ItemStack> nonnulllist = super.getRemainingItems(p_44004_);
            nonnulllist.set(biIndex, ret);
            return nonnulllist;
        } else if (knifeItem.getAction() == KnifeAction.REMOVE) {
            Player p = ForgeHooks.getCraftingPlayer();
            if (p == null) {
                SynapseMod.getLogger().info("Got remaining items for a remove all modules craft while crafting player was unset! The items will not be recovered.");
                return super.getRemainingItems(p_44004_);
            }
            while (true) {
                ItemStack pop = ((AxonBlockItem) axonBlockItem.getItem()).popModule(out);
                if (pop == null) break;
                if (!p.getInventory().add(pop)) {
                    p.drop(pop, false);
                }
            }
        }
        return super.getRemainingItems(p_44004_);
    }

    public boolean canCraftInDimensions(int p_44128_, int p_44129_) {
        return p_44128_ * p_44129_ >= 2;
    }

    public @NotNull RecipeSerializer<?> getSerializer() {
        return SynapseRecipeSerializerRegistry.ADD_MODULE.get();
    }
}
