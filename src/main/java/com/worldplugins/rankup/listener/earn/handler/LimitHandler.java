package com.worldplugins.rankup.listener.earn.handler;

import com.worldplugins.rankup.config.data.EarnData;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.factory.ShardFactory;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Players;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class LimitHandler implements EarnHandler {
    @Override
    public @Nullable List<ShardEarn> getEarns(@NotNull ConfigModel<EarnData> earnConfig, @NotNull Class<? extends ShardEarn> earnType) {
        return earnConfig.data().getLimitEarnsByType(earnType);
    }

    @Override
    public void handlePhysicSend(
        @NotNull Player player,
        @NotNull ShardFactory shardFactory,
        @NotNull ShardsData.Shard configShard,
        @NotNull Integer amount
    ) {
        Players.giveItems(player, shardFactory.createLimit(configShard.id(), amount));
        respond(player, "Limite-fisico-ganho", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@quantia", NumberFormats.suffixed(amount))
        ));
    }

    @Override
    public void handleVirtualSend(
        @NotNull ShardFactory shardFactory,
        @NotNull ShardsData.Shard configShard,
        @NotNull Integer amount,
        @NotNull RankupPlayer playerModel,
        @NotNull Player player,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ShardCompensation compensation
    ) {
        final int currentLimit = playerModel.getShardLimit(configShard.id());
        final Integer addedLimit = currentLimit + amount > configShard.limit()
            ? configShard.limit() - currentLimit
            : amount;

        playerModel.setShardLimit(configShard.id(), currentLimit + addedLimit);
        respond(player, "Limite-virtual-ganho", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@quantia", NumberFormats.suffixed(addedLimit))
        ));

        if (mainConfig.data().hasLimitCompensation(compensation) && addedLimit < amount) {
            final int compensationAmount = amount - addedLimit;
            Players.giveItems(player, shardFactory.createLimit(configShard.id(), compensationAmount));
            respond(player, "Limite-compensacao", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia", NumberFormats.suffixed(compensationAmount))
            ));
        }
    }
}
