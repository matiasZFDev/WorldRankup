package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.conversation.ShardSellConversation;
import com.worldplugins.rankup.conversation.ShardWithdrawConversation;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.ShardFactory;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.*;
import me.post.lib.view.View;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.context.ClickHandler;
import me.post.lib.view.context.ViewContext;
import me.post.lib.view.context.impl.MapViewContext;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class BagView implements View {
    private final @NotNull MenuModel menuModel;
    private final @NotNull ViewContext viewContext;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConversationProvider conversationProvider;
    private final @NotNull Economy economy;
    private final @NotNull ShardFactory shardFactory;

    public BagView(
        @NotNull MenuModel menuModel,
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConversationProvider conversationProvider,
        @NotNull Economy economy,
        @NotNull ShardFactory shardFactory
    ) {
        this.menuModel = menuModel;
        this.viewContext = new MapViewContext();
        this.shardsConfig = shardsConfig;
        this.playerService = playerService;
        this.mainConfig = mainConfig;
        this.conversationProvider = conversationProvider;
        this.economy = economy;
        this.shardFactory = shardFactory;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final List<Integer> itemSlots = menuModel.data().getData("Slots");
        final ItemDisplay shardDisplay = menuModel.data().getData("Display-fragmento");
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        final String sellBonus;
        final String bonusTag;

        if (mainConfig.data().shardSellOptions() == null) {
            sellBonus = "0";
            bonusTag = "";
        } else {
            final MainData.ShardSellOptions.SellBonus bonus =mainConfig.data()
                .shardSellOptions()
                .getBonus(player);
            sellBonus = bonus == null ? "0" : Numbers.plainFormat(bonus.bonus());
            bonusTag = bonus != null && bonus.tag() != null
                ? bonus.tag()
                : mainConfig.data().shardSellOptions().noGroupTag();
        }

        ConfigContextBuilder.withModel(menuModel)
            .apply(builder -> {
                CollectionHelpers.zip(shardsConfig.data().getAll(), itemSlots).forEach(pair -> {
                    final ShardsData.Shard shard = pair.first();
                    final Double shardPrice = Numbers.applyPercentage(
                        shard.price(), Double.parseDouble(sellBonus), Numbers.ApplyType.INCREMENT
                    );
                    final ItemStack shardItem = ItemTransformer.of(shard.item().clone())
                        .display(shardDisplay)
                        .nameFormat(to("@nome", shard.display()))
                        .loreFormat(
                            to("@quantia", NumberFormats.suffixed(playerModel.getShards(shard.id()))),
                            to("@limite", NumberFormats.suffixed(playerModel.getShardLimit(shard.id()))),
                            to("@max-limite", NumberFormats.suffixed(shard.limit())),
                            to("@bonus-tag", bonusTag),
                            to("@bonus-venda", sellBonus),
                            to("@preco", NumberFormats.suffixed(shardPrice))
                        )
                        .colorMeta()
                        .transform();
                    final byte shardId = shard.id();

                    builder.item(pair.second(), shardItem, click -> {
                        if (shardsConfig.data().getById(shardId) == null) {
                            respond(player, "Fragmento-inexistente");
                            return;
                        }

                        if (click.clickType().isRightClick() && mainConfig.data().isShardWithdrawEnabled()) {
                            player.closeInventory();
                            conversationProvider.create()
                                .withFirstPrompt(new ShardWithdrawConversation(
                                    shardId, playerService, mainConfig, shardsConfig, shardFactory
                                ))
                                .withLocalEcho(false)
                                .withTimeout(20)
                                .buildConversation(player)
                                .begin();
                            return;
                        }

                        if (click.clickType().isLeftClick() && mainConfig.data().shardSellOptions() != null) {
                            player.closeInventory();
                            conversationProvider.create()
                                .withFirstPrompt(new ShardSellConversation(
                                    shardId, playerService, mainConfig, shardsConfig, economy
                                ))
                                .withLocalEcho(false)
                                .withTimeout(20)
                                .buildConversation(player)
                                .begin();
                        }
                    });
                });
            })
            .build(viewContext, player, data);
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
