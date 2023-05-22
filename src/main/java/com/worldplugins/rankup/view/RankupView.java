package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.manager.EvolutionManager;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.view.View;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.helper.ClickHandler;
import me.post.lib.view.helper.ViewContext;
import me.post.lib.view.helper.impl.MapViewContext;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class RankupView implements View {
    private final @NotNull MenuModel menuModel;
    private final @NotNull ViewContext viewContext;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull Economy economy;
    private final @NotNull EvolutionManager evolutionManager;

    public RankupView(
        @NotNull MenuModel menuModel,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull Economy economy,
        @NotNull EvolutionManager evolutionManager
    ) {
        this.menuModel = menuModel;
        this.viewContext = new MapViewContext();
        this.playerService = playerService;
        this.ranksConfig = ranksConfig;
        this.shardsConfig = shardsConfig;
        this.economy = economy;
        this.evolutionManager = evolutionManager;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.rank());
        final RanksData.Rank nextRank = ranksConfig.data().getByName(configRank.evolution().nextRankName());

        final String sufficientStatus = menuModel.data().getData("Status-suficiente");
        final String insufficientStatus = menuModel.data().getData("Status-insuficiente");
        final String shardFormat = menuModel.data().getData("Formato-fragmento");
        final String coinsStatus = economy.has(player, configRank.evolution().coinsPrice())
            ? sufficientStatus
            : insufficientStatus;

        ConfigContextBuilder.withModel(menuModel)
            .handleMenuItemClick("Cancelar", click -> {
                player.closeInventory();
                respond(player, "Operacao-cancelada");
            })
            .handleMenuItemClick("Confirmar", this::handleRankEvolution)
            .replaceMenuItem("Info", item -> ItemTransformer.of(nextRank.item().clone())
                    .display(item.getItemMeta())
                    .nameFormat(to("@rank", nextRank.display()))
                    .loreFormat(
                        to("@coins-totais", NumberFormats.suffixed(economy.getBalance(player))),
                        to("@coins-precisas", NumberFormats.suffixed(configRank.evolution().coinsPrice())),
                        to("@coins-status", coinsStatus)
                    )
                    .loreListFormat(
                        "@@fragmentos",
                        configRank.evolution().requiredShards().stream()
                            .map(shard -> {
                                final ShardsData.Shard configShard = shardsConfig.data().getByName(
                                    shard.name()
                                );
                                final String status = playerModel.getShards(configShard.id()) >= shard.amount()
                                    ? sufficientStatus
                                    : insufficientStatus;
                                return Strings.replace(shardFormat,
                                    to("@fragmento", configShard.display()),
                                    to("@quantia", NumberFormats.suffixed(shard.amount())),
                                    to("@status", status)
                                );
                            })
                            .collect(Collectors.toList())
                    )
                    .colorMeta()
                    .transform()
            )
            .build(viewContext, player, data);
    }

    private void handleRankEvolution(@NotNull ViewClick click) {
        final Player player = click.whoClicked();
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.rank());

        player.closeInventory();

        if (configRank.evolution() == null) {
            respond(player, "Rank-ultimo-error");
            return;
        }

        if (!economy.has(player, configRank.evolution().coinsPrice())) {
            respond(player, "Rank-evoluir-dinheiro-insuficiente");
            return;
        }

        final boolean hasShardRequirements = configRank.evolution().requiredShards().stream()
            .allMatch(shard -> {
                final byte shardId = shardsConfig.data().getByName(shard.name()).id();
                return playerModel.getShards(shardId) >= shard.amount();
            });

        if (!hasShardRequirements) {
            respond(player, "Rank-evoluir-fragmentos-insuficientes");
            return;
        }

        final RanksData.Rank nextRank = ranksConfig.data().getByName(configRank.evolution().nextRankName());

        evolutionManager.setRank(player, nextRank.id());
        economy.withdrawPlayer(player, configRank.evolution().coinsPrice());
        configRank.evolution().requiredShards().forEach(shard -> {
            final byte shardId = shardsConfig.data().getByName(shard.name()).id();
            playerModel.setShards(shardId, playerModel.getShards(shardId) - shard.amount());
        });

        if (configRank.evolution().consoleCommand() != null)
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                configRank.evolution().consoleCommand().replace("@jogador", player.getName())
            );

        if (nextRank.evolution() != null) {
            respond(player, "Rank-evoluido", message -> message.replace(
                to("@jogador", player.getName()),
                to("@rank", nextRank.display())
            ));
        } else {
            respond(player, "Rank-evoluido-ultimo", message -> message.replace(
                to("@jogador", player.getName()),
                to("@rank", nextRank.display())
            ));
        }
    }

    @Override
    public void onClick(@NotNull ViewClick click) {
        ClickHandler.handleTopNonNull(viewContext, click);
    }

    @Override
    public void onClose(@NotNull ViewClose close) {
        viewContext.removeViewer(close.whoCloses().getUniqueId());
    }
}
