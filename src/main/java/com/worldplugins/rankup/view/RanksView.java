package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.CollectionExtensions;
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
import com.worldplugins.rankup.config.menu.RanksMenuContainer;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ViewExtensions;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ReplaceExtensions.class,
    GenericExtensions.class,
    ItemExtensions.class,
    CollectionExtensions.class,
    ViewExtensions.class,
    NumberFormatExtensions.class,
    ReplaceExtensions.class
})

@ViewSpec(menuContainer = RanksMenuContainer.class)
@RequiredArgsConstructor
public class RanksView extends MenuDataView<RanksView.Context> {
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Context implements ViewContext {
        private final byte page;
    }

    private final @NonNull ConfigCache<RanksData> ranksConfig;
    private final @NonNull ConfigCache<ShardsData> shardsConfig;
    private final @NonNull PlayerService playerService;

    @Override
    public Context defaultData() {
        return new Context((byte) 0);
    }

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, Context context, @NonNull MenuData menuData) {
        final List<Integer> slots = menuData.getData("Slots");
        final int slotsPerPage = slots.size();
        final int ranksCount = ranksConfig.data().all().size();
        final int totalPages = ranksCount <= slotsPerPage ? 1 : (ranksCount / slotsPerPage) + 1;
        final ItemStack achievedRank = menuData.getData("Iten-rank-atingido");
        final ItemStack unachievedRank = menuData.getData("Iten-rank");
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final String shardFormat = menuData.getData("Fragmento-formato");

        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            if (context.page == 0)
                session.remove("Pagina-anterior");

            if (context.page == totalPages - 1)
                session.remove("Pagina-seguinte");

            session.addDynamics(() -> ranksConfig.data().all().stream()
                .skip((long) context.page * slotsPerPage)
                .limit(slotsPerPage)
                .collect(Collectors.toList())
                .zip(slots).stream()
                .map(rankPair -> {
                    final RanksData.Rank previous = ranksConfig.data().getPrevious(rankPair.first().getId());

                    if (previous == null) {
                        final ItemStack item = rankPair.first().getItem()
                            .display(unachievedRank)
                            .nameFormat("@nome".to(rankPair.first().getDisplay()))
                            .loreFormat("@dinheiro".to("0"))
                            .loreListFormat("@@fragmentos", Collections.emptyList())
                            .inPlaceColorMeta();
                        return new MenuItem("Rank", rankPair.second(), item, null);
                    }

                    final ItemStack item = playerModel.getRank() < rankPair.first().getId()
                        ? rankPair.first().getItem()
                            .display(unachievedRank)
                            .nameFormat("@nome".to(rankPair.first().getDisplay()))
                            .loreFormat("@dinheiro".to(((Double) previous.getEvolution().getCoinsPrice()).suffixed()))
                            .loreListFormat("@@fragmentos", previous.getEvolution().getRequiredShards().stream()
                                .map(requirement -> shardFormat.formatReplace(
                                    "@fragmento".to(shardsConfig.data().getByName(requirement.getName()).getDisplay()),
                                    "@quantia".to(((Integer) requirement.getAmount()).suffixed())
                                ))
                                .collect(Collectors.toList())
                            )
                        : rankPair.first().getItem()
                            .display(achievedRank)
                            .nameFormat("@nome".to(rankPair.first().getDisplay()));
                    return new MenuItem("Rank", rankPair.second(), item.inPlaceColorMeta(), null);
                })
                .collect(Collectors.toList()));
        }).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull String getTitle(@NonNull String title, Context data, @NonNull MenuData menuData) {
        final int slotsPerPage = ((List<Integer>) menuData.getData("Slots")).size();
        final int ranksCount = ranksConfig.data().all().size();
        final int totalPages = ranksCount <= slotsPerPage ? 1 : (ranksCount / slotsPerPage) + 1;

        return title.formatReplace(
            "@atual".to(String.valueOf(data.page + 1)),
            "@total".to(String.valueOf(totalPages))
        );
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent event) {
        if (menuItem.getId().equals("Pagina-seguinte")) {
            player.openView(RanksView.class, new Context((byte) (getContext(player).page + 1)));
            return;
        }

        if (menuItem.getId().equals("Pagina-anterior")) {
            player.openView(RanksView.class, new Context((byte) (getContext(player).page - 1)));
        }
    }
}
