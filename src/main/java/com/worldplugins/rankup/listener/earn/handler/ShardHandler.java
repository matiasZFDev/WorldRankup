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

public class ShardHandler implements EarnHandler {
    @Override
    public @Nullable List<ShardEarn> getEarns(@NotNull ConfigModel<EarnData> earnConfig, @NotNull Class<? extends ShardEarn> earnType) {
        return earnConfig.data().getShardEarnsByType(earnType);
    }

    @Override
    public void handlePhysicSend(
        @NotNull Player player,
        @NotNull ShardFactory shardFactory,
        @NotNull ShardsData.Shard configShard,
        @NotNull Integer amount
    ) {
        Players.giveItems(player, shardFactory.createShard(configShard.id(), amount));
        respond(player, "Fragmento-fisico-ganho", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@quantia", NumberFormats.suffixed(amount))
        ));
    }

    @Override
    public void handleVirtualSend(
        @NotNull ShardFactory shardFactory,
        @NotNull ShardsData.@NotNull Shard configShard,
        @NotNull Integer amount,
        @NotNull RankupPlayer playerModel,
        @NotNull Player player,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ShardCompensation compensation
    ) {
        final byte shardId = configShard.id();
        final int currentAmount = playerModel.getShards(shardId);
        final int shardLimit = playerModel.getShardLimit(shardId);
        final int setAmount = playerModel.setShards(shardId, currentAmount + amount);
        final Integer addedAmount = currentAmount + amount > setAmount
            ? setAmount - currentAmount
            : amount;

        respond(player, "Fragmento-virtual-ganho", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@quantia", NumberFormats.suffixed(addedAmount))
        ));

        if (mainConfig.data().hasShardCompensation(compensation) && currentAmount + amount > shardLimit) {
            final int compensationAmount = currentAmount + amount - shardLimit;
            Players.giveItems(player, shardFactory.createShard(configShard.id(), compensationAmount));
            respond(player, "Fragmentos-compensacao", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia", NumberFormats.suffixed(addedAmount))
            ));
        }
    }
}
