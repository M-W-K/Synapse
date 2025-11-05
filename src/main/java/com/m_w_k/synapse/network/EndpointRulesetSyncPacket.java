package com.m_w_k.synapse.network;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.OldAxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.AxonType;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

import java.util.Optional;
import java.util.function.Consumer;

public class EndpointRulesetSyncPacket {

    protected final AxonType type;
    protected final Either<Consumer<TransferRuleset>, Consumer<FriendlyByteBuf>> syncAction;

    protected final ResourceLocation device;
    protected final Dist destination;

    public EndpointRulesetSyncPacket(AxonType type, Consumer<FriendlyByteBuf> sync, Dist destination, ResourceLocation device) {
        this.type = type;
        this.syncAction = Either.right(sync);
        this.destination = destination;
        this.device = device;
    }
    public EndpointRulesetSyncPacket(FriendlyByteBuf buf) {
        type = buf.readEnum(AxonType.class);
        destination = buf.readEnum(Dist.class);
        TransferRuleset ruleset = AxonDeviceDefinitions.ENDPOINT_RULES.get(type);
        if (ruleset != null) {
            syncAction = Either.left(ruleset.syncAction(buf, destination));
        } else {
            syncAction = Either.right(r -> {});
        }
        device = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeEnum(destination);
        syncAction.ifRight(c -> c.accept(buf));
        buf.writeResourceLocation(device);
    }

    public AxonType getType() {
        return type;
    }

    public Optional<Consumer<TransferRuleset>> syncAction() {
        return syncAction.left();
    }

    public Dist getDestination() {
        return destination;
    }

    public ResourceLocation getDevice() {
        return device;
    }
}
