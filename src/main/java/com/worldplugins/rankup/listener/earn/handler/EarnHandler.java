package com.worldplugins.rankup.listener.earn.handler;

import com.worldplugins.rankup.config.data.EarnData;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.factory.ShardFactory;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface EarnHandler {
    @Nullable List<ShardEarn> getEarns(@NotNull ConfigModel<EarnData> earnConfig, @NotNull Class<? extends ShardEarn> earnType);

    void handlePhysicSend(
        @NotNull Player player,
        @NotNull ShardFactory shardFactory,
        @NotNull ShardsData.Shard configShard,
        @NotNull Integer amount
    );

    void handleVirtualSend(
        @NotNull ShardFactory shardFactory,
        @NotNull ShardsData.Shard configShard,
        @NotNull Integer amount,
        @NotNull RankupPlayer playerModel,
        @NotNull Player player,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ShardCompensation compensation
    );
}
