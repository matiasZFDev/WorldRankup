package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemBuilding;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.manager.EvolutionManager;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Items;
import me.post.lib.view.View;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.helper.ClickHandler;
import me.post.lib.view.helper.ViewContext;
import me.post.lib.view.helper.impl.MapViewContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class PrestigeView implements View {
    private final @NotNull MenuModel menuModel;
    private final @NotNull ViewContext viewContext;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;
    private final @NotNull EvolutionManager evolutionManager;

    public PrestigeView(
        @NotNull MenuModel menuModel,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<PrestigeData> prestigeConfig,
        @NotNull EvolutionManager evolutionManager
    ) {
        this.menuModel = menuModel;
        this.viewContext = new MapViewContext();
        this.playerService = playerService;
        this.ranksConfig = ranksConfig;
        this.prestigeConfig = prestigeConfig;
        this.evolutionManager = evolutionManager;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.rank());
        final Prestige configPrestige = prestigeConfig.data().prestiges().getById(playerModel.prestige());

        ConfigContextBuilder.withModel(menuModel)
            .apply(builder -> {
                if (configPrestige.next() == null) {
                    builder.removeMenuItem("Prestigio-habilitado", "Prestigio-desabilitado");
                    builder.editMenuItem("Prestigio-ultimo", item -> {
                        ItemBuilding.nameFormat(item, to("@prestigio", configPrestige.display()));
                        Items.colorMeta(item);
                    });
                    return;
                }

                final AtomicInteger rankOffsetDistance = new AtomicInteger(0);
                RanksData.Rank currentConfigRank = configRank;

                while (currentConfigRank.evolution() != null) {
                    rankOffsetDistance.incrementAndGet();
                    currentConfigRank = ranksConfig.data().getByName(currentConfigRank.evolution().nextRankName());
                }

                if (rankOffsetDistance.get() == 0) {
                    builder.removeMenuItem("Prestigio-desabilitado", "Prestigio-ultimo");
                    builder.editMenuItem("Prestigio-habilitado", item -> {
                        ItemBuilding.nameFormat(item, to("@prestigio", configPrestige.display()));
                        Items.colorMeta(item);
                    });
                    builder.handleMenuItemClick("Prestigio-habilitado", this::handlePrestige);
                } else {
                    builder.removeMenuItem("Prestigio-habilitado", "Prestigio-ultimo");
                    builder.editMenuItem("Prestigio-desabilitado", item -> {
                        ItemBuilding.nameFormat(item, to("@prestigio", configPrestige.display()));
                        ItemBuilding.loreFormat(item, to("@ranks", rankOffsetDistance.toString()));
                        Items.colorMeta(item);
                    });
                }
            })
            .build(viewContext, player, data);
    }

    private void handlePrestige(@NotNull ViewClick click) {
        final Player player = click.whoClicked();
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.rank());

        if (configRank.evolution() != null) {
            player.closeInventory();
            respond(player, "Prestigio-rank-error"); // error de atualização
            return;
        }

        final Prestige configPrestige = prestigeConfig.data().prestiges().getById(playerModel.prestige());

        if (configPrestige.next() == null) {
            player.closeInventory();
            respond(player, "Prestigio-ultimo-error"); // error de atualização
            return;
        }

        final RanksData.Rank firstRank = ranksConfig.data().getByName(ranksConfig.data().defaultRank());
        final Prestige nextPrestige = prestigeConfig.data().prestiges().getById(configPrestige.next());

        evolutionManager.setRank(player, firstRank.id());
        evolutionManager.setPrestige(player, nextPrestige.id());

        if (nextPrestige.next() != null) {
            respond(player, "Prestigio-evoluido", message -> message.replace(
                to("@jogador", player.getName()),
                to("@prestigio", nextPrestige.display())
            ));
        } else {
            respond(player, "Prestigio-evoluido-ultimo", message -> message.replace(
                to("@jogador", player.getName()),
                to("@prestigio", nextPrestige.display())
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
