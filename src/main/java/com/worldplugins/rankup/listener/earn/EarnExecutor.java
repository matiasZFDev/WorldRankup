package com.worldplugins.rankup.listener.earn;

import com.worldplugins.rankup.config.data.EarnData;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import com.worldplugins.rankup.config.data.earn.ShardEarn;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.ShardFactory;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.worldplugins.rankup.Response.respond;

public final class EarnExecutor {
    private final @NotNull PlayerService playerService;
    private final @NotNull ShardFactory shardFactory;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull ConfigModel<EarnData> earnConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public EarnExecutor(
        @NotNull PlayerService playerService,
        @NotNull ShardFactory shardFactory,
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<EarnData> earnConfig,
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.playerService = playerService;
        this.shardFactory = shardFactory;
        this.ranksConfig = ranksConfig;
        this.earnConfig = earnConfig;
        this.shardsConfig = shardsConfig;
        this.mainConfig = mainConfig;
    }

    public void tryEarns(
        @NotNull String worldName,
        @NotNull Player player,
        @NotNull EarnHandlerType type,
        @NotNull Class<? extends ShardEarn> earnType,
        @NotNull Function<ShardEarn, Boolean> laterChecks
    ) {
        final List<ShardEarn> earns = type.getHandler().getEarns(earnConfig, earnType);

        if (earns == null) {
            return;
        }

        System.out.println("wena po barni");

        earns.forEach(earn -> {
            final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

            if (playerModel == null) {
                return;
            }

            final String rankName = ranksConfig.data().getById(playerModel.rank()).name();

            if (!earn.ranks().contains(rankName)) {
                return;
            }

            if (!earn.worlds().contains(worldName)) {
                return;
            }

            if (!laterChecks.apply(earn)) {
                return;
            }

            earn.shards().forEach(shard -> {
                if (((int) Math.floor(Math.random() * (100 / shard.chance()))) != 0)
                    return;

                final ShardsData.Shard configShard = shardsConfig.data().getByName(shard.name());

                if (configShard == null) {
                    respond(player, "Fragmento-consultado-invalido");
                    return;
                }

                if (earn.sendType() == ShardSendType.PHYISIC) {
                    type.getHandler().handlePhysicSend(player, shardFactory, configShard, shard.amount());
                    return;
                }

                if (earn.sendType() == ShardSendType.VIRTUAL) {
                    type.getHandler().handleVirtualSend(
                        shardFactory, configShard, shard.amount(), playerModel, player,
                        mainConfig, earn.compensation()
                    );
                }
            });
        });
    }
}
