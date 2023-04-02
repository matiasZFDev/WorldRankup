package com.worldplugins.rankup.database.model;

import com.google.common.collect.ImmutableList;
import com.worldplugins.lib.extension.CollectionExtensions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;

import java.util.Collection;
import java.util.UUID;

@ExtensionMethod({
    CollectionExtensions.class
})

@AllArgsConstructor
@Getter
public class RankupPlayerImpl implements RankupPlayer {
    private final @NonNull UUID id;
    @Setter
    private short rank;
    @Setter
    private short prestige;
    private final @NonNull Collection<Shard> shards;

    @Override
    public @NonNull Collection<Shard> getAllShards() {
        return ImmutableList.copyOf(shards);
    }

    @Override
    public int getShards(byte shardId) {
        return shards.find(shard -> shard.getId() == shardId).getAmount();
    }

    @Override
    public void setShards(byte shardId, int amount) {
        shards.find(shard -> shard.getId() == shardId).setAmount(amount);
    }

    @Override
    public int getShardLimit(byte shardId) {
        return shards.find(shard -> shard.getId() == shardId).getLimit();
    }

    @Override
    public void setShardLimit(byte shardId, int amount) {
        shards.find(shard -> shard.getId() == shardId).setLimit(amount);
    }
}
