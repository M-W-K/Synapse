package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class BasicRule implements RuleAccess {

    public static final Codec<BasicRule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonAddress.CODEC.fieldOf("address").forGetter(BasicRule::getAddress),
                    Codec.BOOL.fieldOf("matchesIncoming").forGetter(BasicRule::isMatchesIncoming),
                    Codec.BOOL.fieldOf("matchesOutgoing").forGetter(BasicRule::isMatchesOutgoing)
            ).apply(instance, BasicRule::new));

    @NotNull
    protected AxonAddress address;
    protected boolean matchesIncoming;
    protected boolean matchesOutgoing;

    public BasicRule() {
        this.address = new AxonAddress();
    }

    public BasicRule(@NotNull AxonAddress address, boolean matchesIncoming, boolean matchesOutgoing) {
        this.address = address;
        this.matchesIncoming = matchesIncoming;
        this.matchesOutgoing = matchesOutgoing;
    }

    @Override
    public @NotNull AxonAddress getAddress() {
        return address;
    }

    @Override
    public void setAddress(@NotNull AxonAddress address) {
        this.address = address;
    }

    @Override
    public boolean isMatchesIncoming() {
        return matchesIncoming;
    }

    @Override
    public void setMatchesIncoming(boolean matchesIncoming) {
        this.matchesIncoming = matchesIncoming;
    }

    @Override
    public boolean isMatchesOutgoing() {
        return matchesOutgoing;
    }

    @Override
    public void setMatchesOutgoing(boolean matchesOutgoing) {
        this.matchesOutgoing = matchesOutgoing;
    }
}
