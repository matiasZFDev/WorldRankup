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

public class LimitHandler implements EarnHandler {
    @Override
    public List<ShardEarn> getEarns(@NonNull EarnConfig earnConfig, @NonNull Class<? extends ShardEarn> earnType) {
        return earnConfig.get().getLimitEarnsByType(earnType);
    }

    @Override
    public void handlePhysicSend(
        @NonNull Player player,
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsConfig.Config.Shard configShard,
        @NonNull Integer amount
    ) {
        player.giveItems(shardFactory.createLimit(configShard.getId(), amount));
        player.respond("Limite-fisico-ganho", message -> message.replace(
            "@fragmento".to(configShard.getDisplay()),
            "@quantia".to(amount.suffixed())
        ));
    }

    @Override
    public void handleVirtualSend(
        @NonNull ShardFactory shardFactory,
        @NonNull ShardsConfig.Config.Shard configShard,
        @NonNull Integer amount,
        @NonNull RankupPlayer playerModel,
        @NonNull Player player,
        @NonNull MainConfig mainConfig,
        @NonNull ShardCompensation compensation
    ) {
        final int currentLimit = playerModel.getShardLimit(configShard.getId());
        final Integer addedLimit = currentLimit + amount > configShard.getLimit()
            ? configShard.getLimit() - currentLimit
            : amount;
        playerModel.setShardLimit(configShard.getId(), currentLimit + addedLimit);

        player.respond("Limite-virtual-ganho", message -> message.replace(
            "@fragmento".to(configShard.getDisplay()),
            "@quantia".to(addedLimit.suffixed())
        ));

        if (mainConfig.get().hasLimitCompensation(compensation) && addedLimit < amount) {
            final Integer compensationAmount = amount - addedLimit;
            player.respond("Limite-compensacao", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia".to(compensationAmount.suffixed())
            ));
        }
    }
}
