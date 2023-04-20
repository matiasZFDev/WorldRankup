package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.ReplaceExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.menu.RankupMenuContainer;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.manager.EvolutionManager;
import com.worldplugins.rankup.util.BukkitUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.stream.Collectors;

@ExtensionMethod(value = {
    ItemExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    ReplaceExtensions.class,
    ResponseExtensions.class
}, suppressBaseMethods = false)

@ViewSpec(menuContainer = RankupMenuContainer.class)
@RequiredArgsConstructor
public class RankupView extends MenuDataView<ViewContext> {
    private final @NonNull PlayerService playerService;
    private final @NonNull ConfigCache<RanksData> ranksConfig;
    private final @NonNull ConfigCache<ShardsData> shardsConfig;
    private final @NonNull Economy economy;
    private final @NonNull EvolutionManager evolutionManager;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.getRank());
        final RanksData.Rank nextRank = ranksConfig.data().getByName(
            configRank.getEvolution().getNextRankName()
        );

        final String sufficientStatus = menuData.getData("Status-suficiente");
        final String insufficientStatus = menuData.getData("Status-insuficiente");
        final String shardFormat = menuData.getData("Formato-fragmento");
        final String coinsStatus = economy.has(player, configRank.getEvolution().getCoinsPrice())
            ? sufficientStatus
            : insufficientStatus;

        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            session.modify("Info", item ->
                nextRank.getItem().display(item)
                    .nameFormat("@rank".to(nextRank.getDisplay()))
                    .loreFormat(
                        "@coins-totais".to(((Double) economy.getBalance(player)).suffixed()),
                        "@coins-precisas".to(((Double) configRank.getEvolution().getCoinsPrice()).suffixed()),
                        "@coins-status".to(coinsStatus)
                    )
                    .loreListFormat("@@fragmentos",
                        configRank.getEvolution().getRequiredShards().stream()
                            .map(shard -> {
                                final ShardsData.Shard configShard = shardsConfig.data().getByName(
                                    shard.getName()
                                );
                                final String status = playerModel.getShards(configShard.getId()) >= shard.getAmount()
                                    ? sufficientStatus
                                    : insufficientStatus;
                                return shardFormat.formatReplace(
                                    "@fragmento".to(configShard.getDisplay()),
                                    "@quantia".to(((Integer) shard.getAmount()).suffixed()),
                                    "@status".to(status)
                                );
                            })
                            .collect(Collectors.toList())
                    )
                    .colorMeta()
            );
        }).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        if (item.getId().equals("Confirmar")) {
            final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
            final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.getRank());

            player.closeInventory();

            if (configRank.getEvolution() == null) {
                player.respond("Rank-ultimo-error");
                return;
            }

            if (!economy.has(player, configRank.getEvolution().getCoinsPrice())) {
                player.respond("Rank-evoluir-dinheiro-insuficiente");
                return;
            }

            final boolean hasShardRequirements = configRank.getEvolution().getRequiredShards().stream()
                .allMatch(shard -> {
                    final byte shardId = shardsConfig.data().getByName(shard.getName()).getId();
                    return playerModel.getShards(shardId) >= shard.getAmount();
                });

            if (!hasShardRequirements) {
                player.respond("Rank-evoluir-fragmentos-insuficientes");
                return;
            }

            final RanksData.Rank nextRank = ranksConfig.data().getByName(
                configRank.getEvolution().getNextRankName()
            );

            evolutionManager.setRank(player, nextRank.getId());
            economy.withdrawPlayer(player, configRank.getEvolution().getCoinsPrice());
            configRank.getEvolution().getRequiredShards().forEach(shard -> {
                final byte shardId = shardsConfig.data().getByName(shard.getName()).getId();
                playerModel.setShards(shardId, playerModel.getShards(shardId) - shard.getAmount());
            });

            if (configRank.getEvolution().getConsoleCommand() != null)
                BukkitUtils.consoleCommand(
                    configRank.getEvolution().getConsoleCommand().replace("@jogador", player.getName())
                );

            if (nextRank.getEvolution() != null)
                player.respond("Rank-evoluido", message -> message.replace(
                    "@jogador".to(player.getName()),
                    "@rank".to(nextRank.getDisplay())
                ));
            else
                player.respond("Rank-evoluido-ultimo", message -> message.replace(
                    "@jogador".to(player.getName()),
                    "@rank".to(nextRank.getDisplay())
                ));
            return;
        }

        if (item.getId().equals("Cancelar")) {
            player.closeInventory();
            player.respond("Operacao-cancelada");
            return;
        }
    }
}
