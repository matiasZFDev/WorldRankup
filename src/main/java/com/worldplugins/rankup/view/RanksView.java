package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.PageConfigContextBuilder;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.view.View;
import me.post.lib.view.Views;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.helper.ClickHandler;
import me.post.lib.view.helper.ViewContext;
import me.post.lib.view.helper.impl.MapViewContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class RanksView implements View {
    public static class Context {
        private final byte page;

        public Context(byte page) {
            this.page = page;
        }
    }

    private final @NotNull MenuModel menuModel;
    private final @NotNull ViewContext viewContext;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;

    public RanksView(
        @NotNull MenuModel menuModel,
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull PlayerService playerService
    ) {
        this.menuModel = menuModel;
        this.viewContext = new MapViewContext();
        this.ranksConfig = ranksConfig;
        this.shardsConfig = shardsConfig;
        this.playerService = playerService;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final Context context = (Context) requireNonNull(data);

        final ItemStack achievedRank = menuModel.data().getData("Iten-rank-atingido");
        final ItemStack unachievedRank = menuModel.data().getData("Iten-rank");
        final List<Integer> slots = menuModel.data().getData("Slots");
        final String shardFormat = menuModel.data().getData("Fragmento-formato");
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        PageConfigContextBuilder.of(
                menuModel,
                page -> Views.get().open(player, getClass(), new Context((byte) page)),
                context.page
            )
            .editTitle((pageInfo, title) -> Strings.replace(title,
                to("@atual", String.valueOf(pageInfo.page() + 1)),
                to("@total", String.valueOf(pageInfo.totalPages()))
            ))
            .nextPageButtonAs("Pagina-seguinte")
            .previousPageButtonAs("Pagina-anterior")
            .withSlots(slots)
            .fill(
                ranksConfig.data().all(),
                configRank -> {
                    final RanksData.Rank previous = ranksConfig.data().getPrevious(configRank.id());

                    if (previous == null) {
                        return ItemTransformer.of(configRank.item().clone())
                            .display(unachievedRank.getItemMeta())
                            .nameFormat(to("@nome", configRank.display()))
                            .loreFormat(to("@dinheiro", "0"))
                            .loreListFormat("@@fragmentos", Collections.emptyList())
                            .colorMeta()
                            .transform();
                    }

                    return playerModel.rank() < configRank.id()
                        ? ItemTransformer.of(configRank.item().clone())
                            .display(unachievedRank.getItemMeta())
                            .nameFormat(to("@nome", configRank.display()))
                            .loreFormat(to("@dinheiro", NumberFormats.suffixed(previous.evolution().coinsPrice())))
                            .loreListFormat("@@fragmentos", previous.evolution().requiredShards().stream()
                                .map(requirement -> Strings.replace(shardFormat,
                                    to("@fragmento", shardsConfig.data().getByName(requirement.name()).display()),
                                    to("@quantia", NumberFormats.suffixed(requirement.amount()))
                                ))
                                .collect(Collectors.toList())
                            )
                            .colorMeta()
                            .transform()
                        : ItemTransformer.of(configRank.item())
                            .display(achievedRank.getItemMeta())
                            .nameFormat(to("@nome", configRank.display()))
                            .colorMeta()
                            .transform();
                }
            )
            .build(viewContext, player, data);
        /*ConfigContextBuilder.withModel(menuModel)
            .editTitle(title ->
                Strings.replace(title,
                    to("@atual", String.valueOf(context.page + 1)),
                    to("@total", String.valueOf(totalPages))
                )
            )
            .handleMenuItemClick("Pagina-seguinte", click ->
                Views.get().open(player, RanksView.class, new Context((byte) (context.page + 1)))
            )
            .handleMenuItemClick("Pagina-anterior", click ->
                Views.get().open(player, RanksView.class, new Context((byte) (context.page - 1)))
            )
            .apply(builder -> {
                if (context.page == 0) {
                    builder.removeMenuItem("Pagina-anterior");
                }

                if (context.page == totalPages - 1) {
                    builder.removeMenuItem("Pagina-seguinte");
                }

                CollectionHelpers.zip(
                    ranksConfig.data().all().stream()
                        .skip((long) context.page * slotsPerPage)
                        .limit(slotsPerPage)
                        .collect(Collectors.toList()),
                    slots
                ).forEach(rankPair -> {
                    final RanksData.Rank previous = ranksConfig.data().getPrevious(rankPair.first().id());

                    if (previous == null) {
                        builder.item(
                            rankPair.second(),
                            ItemTransformer.of(rankPair.first().item().clone())
                                .display(unachievedRank.getItemMeta())
                                .nameFormat(to("@nome", rankPair.first().display()))
                                .loreFormat(to("@dinheiro", "0"))
                                .loreListFormat("@@fragmentos", Collections.emptyList())
                                .colorMeta()
                                .transform()
                        );
                        return;
                    }

                    final ItemStack item = playerModel.rank() < rankPair.first().id()
                        ? ItemTransformer.of(rankPair.first().item())
                            .display(unachievedRank.getItemMeta())
                            .nameFormat(to("@nome", rankPair.first().display()))
                            .loreFormat(to("@dinheiro", NumberFormats.suffixed(previous.evolution().coinsPrice())))
                            .loreListFormat("@@fragmentos", previous.evolution().requiredShards().stream()
                                .map(requirement -> Strings.replace(shardFormat,
                                    to("@fragmento", shardsConfig.data().getByName(requirement.name()).display()),
                                    to("@quantia", NumberFormats.suffixed(requirement.amount()))
                                ))
                                .collect(Collectors.toList())
                            )
                            .colorMeta()
                            .transform()
                        : ItemTransformer.of(rankPair.first().item())
                            .display(achievedRank.getItemMeta())
                            .nameFormat(to("@nome", rankPair.first().display()))
                            .colorMeta()
                            .transform();
                    builder.item(rankPair.second(), item);
                });
            })
            .build(viewContext, player, data);*/
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
