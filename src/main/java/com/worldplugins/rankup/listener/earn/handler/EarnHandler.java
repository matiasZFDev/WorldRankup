package com.worldplugins.rankup.listener.earn.handler;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.rankup.config.data.EarnData;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.factory.ShardFactory;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.List;

public interface EarnHandler {
    List<ShardEarn> getEarns(
        @NonNull ConfigCache<EarnData> earnConfig,
        @NonNull Class<? extends ShardEarn> earnType
    );


    void handlePhysicSend(
        @NonNull Player player,
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsData.Shard configShard,
        @NonNull Integer amount
    );

    void handleVirtualSend(
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsData.Shard configShard,
        @NonNull Integer amount,
        @NonNull RankupPlayer playerModel,
        @NonNull Player player,
        @NonNull ConfigCache<MainData> mainConfig,
        @NonNull ShardCompensation compensation
    );
}
