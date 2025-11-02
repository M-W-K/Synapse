package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.client.gui.AbstractConnectorScreen;
import com.m_w_k.synapse.client.gui.ruleset.EnergyRulesetWidget;
import com.m_w_k.synapse.client.gui.ruleset.RulesetWidget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class EnergyTransferRuleset implements TransferRuleset.QueryableRuleset<Void> {

    public static final Codec<EnergyTransferRuleset> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(BasicRule.CODEC).fieldOf("rules").forGetter(r -> r.rules)
                    ).apply(instance, EnergyTransferRuleset::new));

    protected final @NotNull List<BasicRule> rules;
    private final Dist dist;

    protected List<Consumer<FriendlyByteBuf>> toSync = new ObjectArrayList<>();

    protected Runnable changeListener = () -> {};

    public EnergyTransferRuleset(Dist dist) {
        this.dist = dist;
        this.rules = new ObjectArrayList<>();
        this.rules.add(defaultRule());
    }

    protected EnergyTransferRuleset(List<BasicRule> rules) {
        this.rules = new ObjectArrayList<>(rules);
        this.dist = Dist.DEDICATED_SERVER;
    }

    protected BasicRule defaultRule() {
        AxonAddress address = new AxonAddress();
        ConnectorLevel.ADDRESS_SPACE.forEach(l -> address.put(l, AxonAddress.WILDCARD));
        address.put(ConnectorLevel.ENDPOINT, AxonAddress.WILDCARD);
        return new BasicRule(address, true, false);
    }

    public void setChangeListener(@NotNull Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public @NotNull RuleAccess ruleAt(int index) {
        if (dist.isClient()) {
            return new ProtectedRuleAccess(rules.get(index), index);
        }
        return rules.get(index);
    }

    protected @NotNull BasicRule actualRuleAt(int index) {
        return rules.get(index);
    }

    public int ruleCount() {
        return rules.size();
    }

    public void applyAction(int index, RuleAction action) {
        switch (action) {
            case SHIFT_LEFT -> {
                BasicRule replace = rules.set(index - 1, rules.get(index));
                rules.set(index, replace);
            }
            case SHIFT_RIGHT -> {
                BasicRule replace = rules.set(index + 1, rules.get(index));
                rules.set(index, replace);
            }
            case DELETE -> {
                rules.remove(index);
                if (rules.isEmpty()) {
                    rules.add(defaultRule());
                }
            }
            case ADD -> {
                rules.add(index, new BasicRule());
            }
        }
        toSync.add(buf -> {
            buf.writeEnum(action);
            buf.writeVarInt(index);
        });
    }

    @Override
    @UnmodifiableView
    public @Nullable AxonAddress getMatchingAddress(@NotNull Void unused, boolean incoming) {
        for (BasicRule rule : rules) {
            if (incoming && !rule.isMatchesIncoming()) continue;
            if (!incoming && !rule.isMatchesOutgoing()) continue;
            return rule.getAddress();
        }
        return null;
    }

    @Override
    public @NotNull Collection<AxonAddress> getAllPossibleAddresses() {
        return rules.stream().map(BasicRule::getAddress).toList();
    }

    @Override
    public @NotNull TransferRuleset createNew(Dist dist) {
        return new EnergyTransferRuleset(dist);
    }

    @Override
    public @NotNull Consumer<TransferRuleset> syncAction(FriendlyByteBuf buf, Dist destination) {
        if (destination.isClient()) {
            int count = buf.readVarInt();
            List<BasicRule> overwrite = new ObjectArrayList<>();
            for (int i = 0; i < count; i++) {
                BasicRule rule = new BasicRule();
                rule.address.read(buf);
                rule.matchesIncoming = buf.readBoolean();
                rule.matchesOutgoing = buf.readBoolean();
                overwrite.add(rule);
            }
            return r -> {
                if (r instanceof EnergyTransferRuleset item) {
                    item.rules.clear();
                    item.rules.addAll(overwrite);
                    item.changeListener.run();
                }
            };
        } else {
            int count = buf.readVarInt();
            final List<Consumer<EnergyTransferRuleset>> actions = new ObjectArrayList<>(count);
            for (int i = 0; i < count; i++) {
                RuleAction action = buf.readEnum(RuleAction.class);
                int index = buf.readVarInt();
                if (action == RuleAction.MODIFY) {
                    var act = buf.readEnum(ProtectedRuleAccess.ModifyType.class);
                    switch (act) {
                        case ADDRESS -> {
                            AxonAddress address = new AxonAddress();
                            address.read(buf);
                            actions.add(r -> r.actualRuleAt(index).setAddress(address));
                        }
                        case INCOMING -> {
                            boolean incoming = buf.readBoolean();
                            actions.add(r -> r.actualRuleAt(index).setMatchesIncoming(incoming));
                        }
                        case OUTGOING -> {
                            boolean outgoing = buf.readBoolean();
                            actions.add(r -> r.actualRuleAt(index).setMatchesOutgoing(outgoing));
                        }
                    }
                } else {
                    actions.add(r -> r.applyAction(index, action));
                }
            }
            return r -> {
                if (r instanceof EnergyTransferRuleset item) {
                    for (Consumer<EnergyTransferRuleset> a : actions) {
                        a.accept(item);
                    }
                    item.changeListener.run();
                }
            };
        }
    }

    @Override
    public @NotNull Consumer<FriendlyByteBuf> clientSyncData() {
        return buf -> {
            buf.writeVarInt(rules.size());
            for (int i = 0; i < rules.size(); i++) {
                BasicRule rule = rules.get(i);
                rule.address.write(buf);
                buf.writeBoolean(rule.matchesIncoming);
                buf.writeBoolean(rule.matchesOutgoing);
            }
        };
    }

    @Override
    public @NotNull AxonType getType() {
        return AxonType.ENERGY;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull Consumer<FriendlyByteBuf> serverSyncData(@NotNull RulesetWidget widget) {
        var list = toSync;
        toSync = new ObjectArrayList<>();
        return buf -> {
            buf.writeVarInt(list.size());
            for (Consumer<FriendlyByteBuf> action : list) {
                action.accept(buf);
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean hasPendingSync(@NotNull RulesetWidget widget) {
        return !toSync.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull RulesetWidget createWidget(AbstractConnectorScreen<?> parent, int x, int y) {
        return new EnergyRulesetWidget(this, parent, x, y);
    }

    public class ProtectedRuleAccess implements RuleAccess {
        protected final @NotNull BasicRule backer;
        protected final int index;

        public ProtectedRuleAccess(@NotNull BasicRule backer, int index) {
            this.backer = backer;
            this.index = index;
        }

        @Override
        public @NotNull AxonAddress getAddress() {
            return backer.getAddress();
        }

        @Override
        public void setAddress(@NotNull AxonAddress address) {
            backer.setAddress(address);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.ADDRESS);
                address.write(buf);
            });
        }

        @Override
        public boolean isMatchesIncoming() {
            return backer.isMatchesIncoming();
        }

        @Override
        public void setMatchesIncoming(boolean matchesIncoming) {
            backer.setMatchesIncoming(matchesIncoming);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.INCOMING);
                buf.writeBoolean(matchesIncoming);
            });
        }

        @Override
        public boolean isMatchesOutgoing() {
            return backer.isMatchesOutgoing();
        }

        @Override
        public void setMatchesOutgoing(boolean matchesOutgoing) {
            backer.setMatchesOutgoing(matchesOutgoing);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.OUTGOING);
                buf.writeBoolean(matchesOutgoing);
            });
        }

        public enum ModifyType {
            ADDRESS, INCOMING, OUTGOING
        }
    }
}
