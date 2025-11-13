package com.m_w_k.synapse.data;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public interface StatePropertiesPredicateBuilderExtension {

    StatePropertiesPredicate.Builder hasRange(Property<Integer> prop, @Nullable Integer min, @Nullable Integer max);
}
