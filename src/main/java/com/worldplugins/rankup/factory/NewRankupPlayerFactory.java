package com.worldplugins.rankup.factory;

import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.model.RankupPlayerImpl;
import com.worldplugins.rankup.database.model.Shard;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NewRankupPlayerFactory implements RankupPlayerFactory {
    private final @NonNull ShardsConfig shardsConfig;

    @Override
    public @NonNull RankupPlayer create(@NonNull UUID playerId) {
        final short rank = 0;
        final short prestige = 0;
        final Collection<Shard> shards = shardsConfig.get().getAll().stream()
            .map(configShard -> new Shard(configShard.getId(), 0, configShard.getDefaultLimit()))
            .collect(Collectors.toList());
        return new RankupPlayerImpl(playerId, rank, prestige, shards);
    }
}
