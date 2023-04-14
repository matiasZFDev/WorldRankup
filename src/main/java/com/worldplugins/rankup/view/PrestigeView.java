package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.ReplaceExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewOf;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.PrestigeConfig;
import com.worldplugins.rankup.config.RanksConfig;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.config.menu.PrestigeMenuContainer;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.manager.EvolutionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.concurrent.atomic.AtomicInteger;

@ExtensionMethod(value = {
    ItemExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    ReplaceExtensions.class,
    ResponseExtensions.class,
    NBTExtensions.class
}, suppressBaseMethods = false)

@ViewOf(menuContainer = PrestigeMenuContainer.class)
@RequiredArgsConstructor
public class PrestigeView extends MenuDataView<ViewContext> {
    private final @NonNull PlayerService playerService;
    private final @NonNull RanksConfig ranksConfig;
    private final @NonNull PrestigeConfig prestigeConfig;
    private final @NonNull EvolutionManager evolutionManager;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final RanksConfig.Config.Rank configRank = ranksConfig.get().getById(playerModel.getRank());
        final Prestige configPrestige = prestigeConfig.get().getPrestiges().getById(playerModel.getPrestige());

        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            if (configPrestige.getNext() == null) {
                session.remove("Prestigio-habilitado", "Prestigio-desabilitado");
                session.modify("Prestigio-ultimo", item ->
                    item.nameFormat("@prestigio".to(configPrestige.getDisplay())).colorMeta()
                );
            } else {
                AtomicInteger rankOffsetDistance = new AtomicInteger(0);
                RanksConfig.Config.Rank currentConfigRank = configRank;

                while (currentConfigRank.getEvolution() != null) {
                    rankOffsetDistance.incrementAndGet();
                    currentConfigRank = ranksConfig.get().getByName(currentConfigRank.getEvolution().getNextRankName());
                }

                if (rankOffsetDistance.get() == 0) {
                    session.remove("Prestigio-desabilitado", "Prestigio-ultimo");
                    session.modify("Prestigio-habilitado", item ->
                        item
                            .nameFormat("@prestigio".to(configPrestige.getDisplay()))
                            .addReferenceId(NBTKeys.PRESTIGE_ENABLED)
                            .colorMeta()
                    );
                } else {
                    session.remove("Prestigio-habilitado", "Prestigio-ultimo");
                    session.modify("Prestigio-desabilitado", item ->
                        item
                            .nameFormat("@prestigio".to(configPrestige.getDisplay()))
                            .loreFormat("@ranks".to(rankOffsetDistance.toString()))
                            .colorMeta()
                    );
                }
            }

        }).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        if (event.getCurrentItem().hasReference(NBTKeys.PRESTIGE_ENABLED)) {
            final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
            final RanksConfig.Config.Rank configRank = ranksConfig.get().getById(playerModel.getRank());

            if (configRank.getEvolution() != null) {
                player.closeInventory();
                player.respond("Prestigio-rank-error"); // error de atualização
                return;
            }

            final Prestige configPrestige = prestigeConfig.get().getPrestiges().getById(playerModel.getPrestige());

            if (configPrestige.getNext() == null) {
                player.closeInventory();
                player.respond("Prestigio-ultimo-error"); // error de atualização
                return;
            }

            final RanksConfig.Config.Rank firstRank = ranksConfig.get().getByName(ranksConfig.get().getDefaultRank());
            final Prestige nextPrestige = prestigeConfig.get().getPrestiges().getById(configPrestige.getNext());

            evolutionManager.setRank(player, firstRank.getId());
            evolutionManager.setPrestige(player, nextPrestige.getId());

            if (nextPrestige.getNext() != null)
                player.respond("Prestigio-evoluido", message -> message.replace(
                    "@jogador".to(player.getName()),
                    "@prestigio".to(nextPrestige.getDisplay())
                ));
            else
                player.respond("Prestigio-evoluido-ultimo", message -> message.replace(
                    "@jogador".to(player.getName()),
                    "@prestigio".to(nextPrestige.getDisplay())
                ));
        }
    }
}
