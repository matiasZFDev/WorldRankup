package com.worldplugins.rankup.view;

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
import com.worldplugins.lib.view.annotation.ViewOf;
import com.worldplugins.rankup.config.RanksConfig;
import com.worldplugins.rankup.config.ShardsConfig;
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

@ViewOf(menuContainer = RankupMenuContainer.class)
@RequiredArgsConstructor
public class RankupView extends MenuDataView<ViewContext> {
    private final @NonNull PlayerService playerService;
    private final @NonNull RanksConfig ranksConfig;
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull Economy economy;
    private final @NonNull EvolutionManager evolutionManager;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksConfig.Config.Rank configRank = ranksConfig.get().getById(playerModel.getRank());
        final RanksConfig.Config.Rank nextRank = ranksConfig.get().getByName(
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
                                final ShardsConfig.Config.Shard configShard = shardsConfig.get().getByName(
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
            final RanksConfig.Config.Rank configRank = ranksConfig.get().getById(playerModel.getRank());

            if (configRank.getEvolution() == null) {
                player.closeInventory();
                player.respond("Rank-ultimo-error");
                return;
            }

            if (!economy.has(player, configRank.getEvolution().getCoinsPrice())) {
                player.closeInventory();
                player.respond("Rank-evoluir-dinheiro-insuficiente");
                return;
            }

            final boolean hasShardRequirements = configRank.getEvolution().getRequiredShards().stream()
                .allMatch(shard -> {
                    final byte shardId = shardsConfig.get().getByName(shard.getName()).getId();
                    return playerModel.getShards(shardId) >= shard.getAmount();
                });

            if (!hasShardRequirements) {
                player.closeInventory();
                player.respond("Rank-evoluir-fragmentos-insuficientes");
                return;
            }

            final RanksConfig.Config.Rank nextRank = ranksConfig.get().getByName(
                configRank.getEvolution().getNextRankName()
            );

            evolutionManager.setRank(player, nextRank.getId());

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
