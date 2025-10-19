package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import org.jetbrains.annotations.NotNull;

public interface RuleAccess {

    @NotNull AxonAddress getAddress();

    void setAddress(@NotNull AxonAddress address);

    boolean isMatchesIncoming();

    void setMatchesIncoming(boolean matchesIncoming);

    boolean isMatchesOutgoing();

    void setMatchesOutgoing(boolean matchesOutgoing);
}
