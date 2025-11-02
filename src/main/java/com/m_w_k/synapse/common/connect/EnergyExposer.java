package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.IFacedAxonBlockEntity;
import com.m_w_k.synapse.api.block.ruleset.EnergyTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class EnergyExposer extends AbstractExposer<IEnergyStorage, EnergyExposer, Void> implements IEnergyStorage {

    protected EnergyTransferRuleset ruleset = new EnergyTransferRuleset(Dist.DEDICATED_SERVER);

    public EnergyExposer(@NotNull IFacedAxonBlockEntity owner) {
        super(owner);
    }

    protected EnergyExposer(@NotNull Direction dir, @NotNull EnergyExposer parent) {
        super(dir, parent);
    }

    @Override
    protected EnergyExposer constructChild(Direction dir) {
        if (dir == null) return this;
        return new EnergyExposer(dir, this);
    }

    @Override
    protected @NotNull Tag save() {
        return EnergyTransferRuleset.CODEC.encodeStart(NbtOps.INSTANCE, this.ruleset).get()
                .map(UnaryOperator.identity(), e -> ByteTag.valueOf((byte) 0));
    }

    @Override
    protected void load(@NotNull Tag nbt) {
        EnergyTransferRuleset.CODEC.parse(NbtOps.INSTANCE, nbt).get()
                .ifLeft(ruleset -> {
                    this.ruleset = ruleset;
                    ruleset.setChangeListener(this::setDirty);
                });
    }

    @Override
    public TransferRuleset.@Nullable QueryableRuleset<Void> getRuleset() {
        return ruleset;
    }

    @Override
    protected AxonType getType() {
        return AxonType.ENERGY;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accept = 0;
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            accept += connection.capability().receiveEnergy(maxReceive - accept, simulate);
        }
        return accept;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int accept = 0;
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            accept += connection.capability().extractEnergy(maxExtract - accept, simulate);
        }
        return accept;
    }

    @Override
    public int getEnergyStored() {
        return getConnections().stream().map(AxonTree.Connection::capability).filter(Objects::nonNull)
                .mapToInt(IEnergyStorage::getEnergyStored).sum();
    }

    @Override
    public int getMaxEnergyStored() {
        return getConnections().stream().map(AxonTree.Connection::capability).filter(Objects::nonNull)
                .mapToInt(IEnergyStorage::getMaxEnergyStored).sum();
    }

    @Override
    public boolean canExtract() {
        return getConnections().stream().map(AxonTree.Connection::capability).filter(Objects::nonNull)
                .anyMatch(IEnergyStorage::canExtract);
    }

    @Override
    public boolean canReceive() {
        return getConnections().stream().map(AxonTree.Connection::capability).filter(Objects::nonNull)
                .anyMatch(IEnergyStorage::canReceive);
    }
}
