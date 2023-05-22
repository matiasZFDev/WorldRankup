package com.worldplugins.rankup.database.model;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class RankupPlayerImpl implements RankupPlayer {
    private final @NotNull UUID id;
    private short rank;
    private short prestige;
    private final @NotNull Collection<Shard> shards;
    private boolean updated;

    public RankupPlayerImpl(@NotNull UUID id, short rank, short prestige, @NotNull Collection<Shard> shards) {
        this.id = id;
        this.rank = rank;
        this.prestige = prestige;
        this.shards = shards;
        this.updated = false;
    }

    public @NotNull UUID id() {
        return id;
    }

    public short rank() {
        return rank;
    }

    public short prestige() {
        return prestige;
    }

    @Override
    public @NotNull Collection<Shard> getAllShards() {
        return ImmutableList.copyOf(shards);
    }

    @Override
    public void setRank(short rank) {
        this.rank = rank;
        updated = true;
    }

    @Override
    public void setPrestige(short prestige) {
        this.prestige = prestige;
        updated = true;
    }

    private @NotNull Shard findShardById(byte id) {
        final Shard shard = shards.stream()
            .filter(it -> it.id() == id)
            .findFirst()
            .orElse(null);

        if (shard != null) {
            return shard;
        }

        final Shard newShard = new Shard(id, 0, 0);
        shards.add(newShard);
        return newShard;
    }

    @Override
    public int getShards(byte shardId) {
        return findShardById(shardId).amount();
    }

    @Override
    public int setShards(byte shardId, int amount) {
        final Shard shard = findShardById(shardId);
        updated = true;

        if (amount > shard.limit()) {
            shard.setAmount(shard.limit());
            return shard.limit();
        }

        shard.setAmount(amount);
        return amount;
    }

    @Override
    public int getShardLimit(byte shardId) {
        return findShardById(shardId).limit();
    }

    @Override
    public void setShardLimit(byte shardId, int amount) {
        findShardById(shardId).setLimit(amount);
        updated = true;
    }

    @Override
    public boolean checkUpdateAndReset() {
        final boolean updated = this.updated;
        this.updated = false;
        return updated;
    }
}
