package com.m_w_k.synapse.mixin;

import com.google.common.collect.Lists;
import com.m_w_k.synapse.data.StatePropertiesPredicateBuilderExtension;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(StatePropertiesPredicate.Builder.class)
public abstract class StatePropertiesPredicateBuilderMixin implements StatePropertiesPredicateBuilderExtension {
    @Final
    @Shadow
    private List<Object> matchers;

    @Override
    public StatePropertiesPredicate.Builder hasRange(Property<Integer> prop, @Nullable Integer min, @Nullable Integer max) {
        matchers.add(new StatePropertiesPredicate.RangedPropertyMatcher(prop.getName(),
                min == null ? null : min.toString(),
                max == null ? null : max.toString()));
        return (StatePropertiesPredicate.Builder) (Object) this;
    }
}
