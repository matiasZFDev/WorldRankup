package com.worldplugins.rankup.database.model;

import com.google.common.collect.ImmutableList;
import com.worldplugins.lib.extension.CollectionExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.Collection;
import java.util.UUID;

@ExtensionMethod({
    CollectionExtensions.class
})

public class RankupPlayerImpl implements RankupPlayer {
    @Getter
    private final @NonNull UUID id;
    @Getter
    private short rank;
    @Getter
    private short prestige;
    private final @NonNull Collection<Shard> shards;
    private boolean updated;

    public RankupPlayerImpl(@NonNull UUID id, short rank, short prestige, @NonNull Collection<Shard> shards) {
        this.id = id;
        this.rank = rank;
        this.prestige = prestige;
        this.shards = shards;
        this.updated = false;
    }

    @Override
    public @NonNull Collection<Shard> getAllShards() {
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

    @Override
    public int getShards(byte shardId) {
        return shards.find(shard -> shard.getId() == shardId).getAmount();
    }

    @Override
    public int setShards(byte shardId, int amount) {
        final Shard shard = shards.find(current -> current.getId() == shardId);
        updated = true;

        if (amount > shard.getLimit()) {
            shard.setAmount(shard.getLimit());
            return shard.getLimit();
        }

        shard.setAmount(amount);
        return amount;
    }

    @Override
    public int getShardLimit(byte shardId) {
        return shards.find(shard -> shard.getId() == shardId).getLimit();
    }

    @Override
    public void setShardLimit(byte shardId, int amount) {
        shards.find(shard -> shard.getId() == shardId).setLimit(amount);
        updated = true;
    }

    @Override
    public boolean checkUpdateAndReset() {
        final boolean updated = this.updated;
        this.updated = false;
        return updated;
    }
}
