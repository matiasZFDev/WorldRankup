package com.worldplugins.rankup.listener.earn.handler;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.config.EarnConfig;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.config.data.ShardCompensation;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.factory.ShardFactory;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import java.util.List;

@ExtensionMethod(value = {
    PlayerExtensions.class,
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class
}, suppressBaseMethods = false)

public class ShardHandler implements EarnHandler {
    @Override
    public List<ShardEarn> getEarns(@NonNull EarnConfig earnConfig, @NonNull Class<? extends ShardEarn> earnType) {
        return earnConfig.get().getShardEarnsByType(earnType);
    }

    @Override
    public void handlePhysicSend(
        @NonNull Player player,
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsConfig.Config.Shard configShard,
        @NonNull Integer amount
    ) {
        player.giveItems(shardFactory.createShard(configShard.getId(), amount));
        player.respond("Fragmento-fisico-ganho", message -> message.replace(
            "@fragmento".to(configShard.getDisplay()),
            "@quantia".to(amount.suffixed())
        ));
    }

    @Override
    public void handleVirtualSend(
        @NonNull ShardFactory shardFactory,
        ShardsConfig.Config.@NonNull Shard configShard,
        @NonNull Integer amount,
        @NonNull RankupPlayer playerModel,
        @NonNull Player player,
        @NonNull MainConfig mainConfig,
        @NonNull ShardCompensation compensation
    ) {
        final byte shardId = configShard.getId();
        final int currentAmount = playerModel.getShards(shardId);
        final int setAmount = playerModel.setShards(shardId, currentAmount + amount);
        final Integer addedAmount = currentAmount + amount > setAmount
            ? setAmount - currentAmount
            : amount;

        player.respond("Fragmento-virtual-ganho", message -> message.replace(
            "@fragmento".to(configShard.getDisplay()),
            "@quantia".to(addedAmount.suffixed())
        ));

        if (mainConfig.get().hasShardCompensation(compensation)) {
            player.respond("Fragmentos-compensacao", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia".to(addedAmount.suffixed())
            ));
        }
    }
}
