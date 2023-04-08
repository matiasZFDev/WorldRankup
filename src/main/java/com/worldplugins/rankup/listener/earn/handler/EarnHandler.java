package com.worldplugins.rankup.listener.earn.handler;

import com.worldplugins.rankup.config.EarnConfig;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.config.data.ShardCompensation;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.ShardFactory;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.List;

public interface EarnHandler {
    List<ShardEarn> getEarns(
        @NonNull EarnConfig earnConfig,
        @NonNull Class<? extends ShardEarn> earnType
    );


    void handlePhysicSend(
        @NonNull Player player,
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsConfig.Config.Shard configShard,
        @NonNull Integer amount
    );

    void handleVirtualSend(
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsConfig.Config.Shard configShard,
        @NonNull Integer amount,
        @NonNull RankupPlayer playerModel,
        @NonNull Player player,
        @NonNull MainConfig mainConfig,
        @NonNull ShardCompensation compensation
    );
}
