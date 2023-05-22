package com.worldplugins.rankup.factory;

import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.model.RankupPlayerImpl;
import com.worldplugins.rankup.database.model.Shard;
import me.post.lib.config.model.ConfigModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class NewRankupPlayerFactory implements RankupPlayerFactory {
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;

    public NewRankupPlayerFactory(
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<PrestigeData> prestigeConfig,
        @NotNull ConfigModel<ShardsData> shardsConfig
    ) {
        this.ranksConfig = ranksConfig;
        this.prestigeConfig = prestigeConfig;
        this.shardsConfig = shardsConfig;
    }

    @Override
    public @NotNull RankupPlayer create(@NotNull UUID playerId) {
        final short rank = ranksConfig.data().getByName(ranksConfig.data().defaultRank()).id();
        final short prestige = prestigeConfig.data().defaulPrestige();
        final Collection<Shard> shards = shardsConfig.data().getAll().stream()
            .map(configShard -> new Shard(configShard.id(), 0, configShard.defaultLimit()))
            .collect(Collectors.toList());
        return new RankupPlayerImpl(playerId, rank, prestige, shards);
    }
}
