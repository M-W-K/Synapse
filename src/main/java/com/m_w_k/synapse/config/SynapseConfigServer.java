package com.m_w_k.synapse.config;

public class SynapseConfigServer extends ConfigBase {

    public final SynapseConfigNetwork network = nested(0, SynapseConfigNetwork::new, "Rules for networks created with Synapse");

    @Override
    public String getName() {
        return "server";
    }
}
