package com.worldplugins.rankup.factory;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.ShardsData;
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
    private final @NonNull ConfigCache<RanksData> ranksConfig;
    private final @NonNull ConfigCache<PrestigeData> prestigeConfig;
    private final @NonNull ConfigCache<ShardsData> shardsConfig;

    @Override
    public @NonNull RankupPlayer create(@NonNull UUID playerId) {
        final short rank = ranksConfig.data().getByName(ranksConfig.data().getDefaultRank()).getId();
        final short prestige = prestigeConfig.data().getDefaulPrestige();
        final Collection<Shard> shards = shardsConfig.data().getAll().stream()
            .map(configShard -> new Shard(configShard.getId(), 0, configShard.getDefaultLimit()))
            .collect(Collectors.toList());
        return new RankupPlayerImpl(playerId, rank, prestige, shards);
    }
}
