package com.worldplugins.rankup.listener.earn;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.rankup.config.EarnConfig;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.config.data.ShardSendType;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.factory.ShardFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import java.util.function.Function;

@ExtensionMethod({
    GenericExtensions.class,
    ResponseExtensions.class
})

@RequiredArgsConstructor
public final class EarnExecutor {
    private final @NonNull EarnConfig earnConfig;
    private final @NonNull ShardFactory shardFactory;
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull PlayerService playerService;
    private final @NonNull MainConfig mainConfig;

    public void tryEarns(
        @NonNull String worldName,
        @NonNull Player player,
        @NonNull EarnHandlerType type,
        @NonNull Class<? extends ShardEarn> earnType,
        @NonNull Function<ShardEarn, Boolean> laterChecks
    ) {
        type.getHandler().getEarns(earnConfig, earnType).ifNotNull(earns -> earns.forEach(earn -> {
            final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

            if (playerModel == null)
                return;

            final String rankName = String.valueOf(playerModel.getRank());

            if (!earn.getRanks().contains(rankName))
                return;

            if (!earn.getWorlds().contains(worldName))
                return;

            if (!laterChecks.apply(earn))
                return;

            earn.getShards().forEach(shard -> {
                if (((int) Math.floor(Math.random() * (100 / shard.getChance()))) != 0)
                    return;

                final ShardsConfig.Config.Shard configShard = shardsConfig.get().getByName(shard.getName());

                if (configShard == null) {
                    player.respond("Fragmento-consultado-invalido");
                    return;
                }

                if (earn.getSendType() == ShardSendType.PHYISIC) {
                    type.getHandler().handlePhysicSend(player, shardFactory, configShard, shard.getAmount());
                    return;
                }

                if (earn.getSendType() == ShardSendType.VIRTUAL) {
                    type.getHandler().handleVirtualSend(
                        shardFactory, configShard, shard.getAmount(), playerModel, player,
                        mainConfig, earn.getCompensation()
                    );
                }
            });
        }));
    }
}
