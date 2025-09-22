package com.m_w_k.synapse.api.connect;

import com.m_w_k.synapse.common.connect.AxonConnection;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class AxonTree<T> extends SavedData {

    private final @NotNull AxonType type;
    private final @NotNull Map<UUID, ConnectionData> members;

    public static <T> @Nullable AxonTree<T> load(@NotNull Level level, @NotNull AxonType type, Capability<T> cap) {
        if (!(level instanceof ServerLevel s)) return null;
        return s.getServer().overworld().getDataStorage().computeIfAbsent(t -> new AxonTree<>(type, cap, t), () -> new AxonTree<>(type, cap), type.getSerializedName());
    }

    protected AxonTree(@NotNull AxonType type, Capability<T> cap) {
        if (cap != type.getCapability()) throw new IllegalArgumentException("Capability must match type capability!");
        this.type = type;
        this.members = new Object2ObjectOpenHashMap<>();
    }

    /**
     * Ensures the particular UUID is associated with a connection data, and that connection data
     * is associated with the given capability. Address and level will not be overridden if the UUID
     * has already been registered.
     * @param uuid the UUID to register
     * @param address the address to register, if not already registered.
     *                Will be copied to prevent spooky action at a distance.
     * @param level the connection level to register, if not already registered.
     * @param cap the capability to register.
     * @return the connection data now registered with the provided UUID
     */
    public @NotNull ConnectionData register(@NotNull UUID uuid, @NotNull AxonAddress address, @NotNull ConnectionTier level, @Nullable T cap) {
        ConnectionData data = members.computeIfAbsent(uuid, k -> new ConnectionData(address, level));
        data.setCap(cap);
        return data;
    }

    /**
     * Gets the connection data associated with a particular UUID, if it exists.
     * @param uuid the UUID
     * @return the associated data
     */
    public @Nullable ConnectionData get(@NotNull UUID uuid) {
        return members.get(uuid);
    }

    /**
     * Finds connections matching the given address, starting from the given UUID.
     * @param from the UUID to search from
     * @param seek the address to match
     * @return null if {@code from} is not registered with this tree, otherwise the result of {@link #find(ConnectionData, AxonAddress)}
     */
    public @Nullable List<Connection<T>> find(@NotNull UUID from, @NotNull AxonAddress seek) {
        ConnectionData data = members.get(from);
        if (data == null) return null;
        return find(data, seek);
    }

    /**
     * Finds connections matching the given address, starting from the given connection data.
     * @param from the connection data to search from
     * @param seek the address to match
     * @return connections that match the given address
     */
    public @NotNull List<Connection<T>> find(@NotNull ConnectionData from, @NotNull AxonAddress seek) {
        if (seek.equals(from.address)) {
            return List.of(new Connection<>(from.cap, List.of()));
        }
        List<Connection<T>> out = new ObjectArrayList<>();
        findRecurseOut(from, seek, out, new ObjectArrayList<>(ConnectionTier.values().length), null);
        return out;
    }

    protected void findRecurseOut(@NotNull ConnectionData from, @NotNull AxonAddress seek,
                                  @NotNull List<Connection<T>> out, @NotNull List<AxonConnection> recurseDepth,
                                  @Nullable ConnectionData previous) {
        if (seek.matchesAtAndAbove(from.address, from.level)) {
            // short circuit inward if 'from' matches the seek
            findRecurseIn(from, seek, out, recurseDepth, previous);
        }
        ConnectionData upstream = from.getUpstream();
        if (upstream != null) {
            recurseDepth.add(from.upstreamConnection);
            // skip 'from' when we recurse inward from further out
            findRecurseOut(upstream, seek, out, recurseDepth, from);
        }
    }

    protected void findRecurseIn(@NotNull ConnectionData from, @NotNull AxonAddress seek,
                                 @NotNull List<Connection<T>> out, @NotNull List<AxonConnection> recurseDepth,
                                 @Nullable ConnectionData visitedDownstream) {
        recurseDepth.add(from.upstreamConnection);
        if (seek.matches(from.address)) {
            out.add(new Connection<>(from.getCap(), recurseDepth));
        }
        for (Iterator<ConnectionData> it = from.downstream(); it.hasNext(); ) {
            ConnectionData downstream = it.next();
            if (downstream == visitedDownstream) continue;
            if (seek.matchesAtAndAbove(downstream.address, downstream.level)) {
                findRecurseIn(downstream, seek, out, new ObjectArrayList<>(recurseDepth), visitedDownstream);
            }
        }
    }

    protected AxonTree(@NotNull AxonType type, Capability<T> cap, @NotNull CompoundTag tag) {
        this(type, cap);
        int[] uuids = tag.getIntArray("UUIDs");
        ListTag data = tag.getList("Data", Tag.TAG_COMPOUND);
        for (int i = 0; i < uuids.length / 4; i++) {
            UUID id = UUIDUtil.uuidFromIntArray(Arrays.copyOfRange(uuids, 4 * i, 4 * i + 4));
            members.put(id, ConnectionData.from(this, data.getCompound(i)));
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        int[] uuids = new int[4 * members.size()];
        ListTag data = new ListTag();
        int i = 0;
        for (var entry : members.entrySet()) {
            System.arraycopy(UUIDUtil.uuidToIntArray(entry.getKey()), 0, uuids, 4 * i++, 4);
            data.add(entry.getValue().serializeNBT());
        }
        tag.putIntArray("UUIDs", uuids);
        tag.put("Data", data);
        return tag;
    }

    public @NotNull AxonType getType() {
        return type;
    }

    public record Connection<T>(@Nullable T capability, List<AxonConnection> connection) {}

    public final class ConnectionData implements INBTSerializable<CompoundTag> {
        public static final UUID NO_UPSTREAM = new UUID(0, 0);
        public static final AxonAddress err = new AxonAddress();

        public final @NotNull AxonAddress address = new AxonAddress();

        public final @NotNull ConnectionTier level;
        public @Nullable T cap;

        public @NotNull UUID upstream;
        public @NotNull AxonConnection upstreamConnection = new AxonConnection(getType());
        public @Nullable ConnectionData upstreamCache;
        public final @NotNull Map<UUID, ConnectionData> downstream;

        private ConnectionData(@NotNull AxonAddress address, @NotNull ConnectionTier level) {
            this.address.putAll(address);
            this.level = level;
            this.upstream = NO_UPSTREAM;
            downstream = new Reference2ObjectOpenHashMap<>();
        }

        /**
         * Sets the address.
         * @param address the address to overwrite with
         * @apiNote This is done via copying to prevent spooky action at a distance.
         */
        public void setAddress(@NotNull AxonAddress address) {
            this.address.clear();
            this.address.putAll(address);
        }

        public @NotNull AxonAddress getAddress() {
            return address;
        }

        public void setCap(@Nullable T cap) {
            this.cap = cap;
        }

        public @Nullable T getCap() {
            return cap;
        }

        public void setUpstream(@NotNull UUID upstream, @Nullable ConnectionData upstreamCache) {
            this.upstream = upstream;
            this.upstreamCache = upstreamCache;
            this.upstreamConnection = new AxonConnection(getType());
        }

        public @Nullable ConnectionData getUpstream() {
            if (upstream != NO_UPSTREAM && upstreamCache == null) {
                upstreamCache = members.get(upstream);
            }
            return upstreamCache;
        }

        public void addDownstream(@NotNull UUID downstream, @Nullable ConnectionData downstreamCache) {
            this.downstream.put(downstream, downstreamCache);
        }

        public void removeDownstream(@NotNull UUID downstream) {
            this.downstream.remove(downstream);
        }

        public Iterator<ConnectionData> downstream() {
            return new DownstreamIter();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag dat = new CompoundTag();
            AxonAddress.CODEC.encodeStart(NbtOps.INSTANCE, this.address).get()
                    .ifLeft(tag -> dat.put("Address", tag));
            ConnectionTier.CODEC.encodeStart(NbtOps.INSTANCE, this.level).get()
                    .ifLeft(tag -> dat.put("Level", tag));

            if (this.upstream != NO_UPSTREAM) {
                dat.putUUID("Upstream", this.upstream);
                AxonConnection.CODEC.encodeStart(NbtOps.INSTANCE, upstreamConnection).get()
                        .ifLeft(t -> dat.put("Connection", t));

            }
            int[] downstream = new int[4 * this.downstream.size()];
            int j = 0;
            for (var down : this.downstream.entrySet()) {
                System.arraycopy(UUIDUtil.uuidToIntArray(down.getKey()), 0, downstream, 4 * j++, 4);
            }
            dat.putIntArray("Downstream", downstream);
            return dat;
        }

        private static <T> AxonTree<T>.@NotNull ConnectionData from(@NotNull AxonTree<T> tree, @NotNull CompoundTag nbt) {
            AxonTree<T>.ConnectionData d = ConnectionTier.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Level")).result()
                    .map(connectionTier -> tree.new ConnectionData(AxonAddress.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Address"))
                    .result().orElse(err), connectionTier))
                    .orElseGet(() -> tree.new ConnectionData(err, ConnectionTier.RELAY));
            d.deserializeNBT(nbt);
            return d;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.hasUUID("Upstream")) {
                this.upstream = nbt.getUUID("Upstream");
                this.upstreamCache = null;
                AxonConnection.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Connection")).get()
                        .ifLeft(c -> upstreamConnection = c)
                        .ifRight(r -> upstreamConnection = new AxonConnection(getType()));
            }
            int[] downstream = nbt.getIntArray("Downstream");
            for (int j = 0; j < downstream.length / 4; j++) {
                this.downstream.put(UUIDUtil.uuidFromIntArray(Arrays.copyOfRange(downstream, 4 * j, 4 * j + 4)), null);
            }
        }

        private final class DownstreamIter implements Iterator<ConnectionData> {

            final Iterator<Map.Entry<UUID, ConnectionData>> backing = downstream.entrySet().iterator();
            Map.Entry<UUID, ConnectionData> next;

            @Override
            public boolean hasNext() {
                while (next == null && backing.hasNext()) {
                    next = backing.next();
                    if (next.getValue() == null) {
                        ConnectionData cache = members.get(next.getKey());
                        if (cache == null) {
                            backing.remove();
                            next = null;
                        } else {
                            next.setValue(cache);
                        }
                    }
                }
                return next != null;
            }

            @Override
            public ConnectionData next() {
                if (!hasNext()) throw new NoSuchElementException();
                ConnectionData n = next.getValue();
                next = null;
                return n;
            }
        }
    }
}
