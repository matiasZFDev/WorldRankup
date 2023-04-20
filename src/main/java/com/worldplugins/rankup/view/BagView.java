package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.lib.util.ConversationProvider;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.menu.BagMenuContainer;
import com.worldplugins.rankup.conversation.ShardSellConversation;
import com.worldplugins.rankup.conversation.ShardWithdrawConversation;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.factory.ShardFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    CollectionExtensions.class,
    ItemExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    NBTExtensions.class,
    ResponseExtensions.class,
    NumberExtensions.class
})

@ViewSpec(menuContainer = BagMenuContainer.class)
@RequiredArgsConstructor
public class BagView extends MenuDataView<ViewContext> {
    private final @NonNull ConfigCache<ShardsData> shardsConfig;
    private final @NonNull PlayerService playerService;
    private final @NonNull ConfigCache<MainData> mainConfig;
    private final @NonNull ConversationProvider conversationProvider;
    private final Economy economy;
    private final @NonNull ShardFactory shardFactory;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final List<Integer> itemSlots = menuData.getData("Slots");
        final ItemDisplay shardDisplay = menuData.getData("Display-fragmento");
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        final String sellBonus;
        final String bonusTag;

        if (mainConfig.data().getShardSellOptions() == null) {
            sellBonus = "0";
            bonusTag = "";
        } else {
            final MainData.ShardSellOptions.SellBonus bonus =mainConfig.data()
                .getShardSellOptions()
                .getBonus(player);
            sellBonus = bonus == null ? "0" : ((Double) bonus.getBonus()).plainFormat();
            bonusTag = bonus != null && bonus.getTag() != null
                ? bonus.getTag()
                : mainConfig.data().getShardSellOptions().getNoGroupTag();
        }

        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            session.addDynamics(() -> {
                return shardsConfig.data().getAll().zip(itemSlots).stream()
                    .map(pair -> {
                        final ShardsData.Shard shard = pair.first();
                        final Double shardPrice = ((Double) shard.getPrice()).applyPercentage(
                            Double.parseDouble(sellBonus), NumberExtensions.ApplyType.INCREMENT
                        );
                        final ItemStack shardItem = shard.getItem()
                            .display(shardDisplay)
                            .nameFormat("@nome".to(shard.getDisplay()))
                            .loreFormat(
                                "@quantia".to(((Integer) playerModel.getShards(shard.getId())).suffixed()),
                                "@limite".to(((Integer) playerModel.getShardLimit(shard.getId())).suffixed()),
                                "@limite-max".to(((Integer) shard.getLimit()).suffixed()),
                                "@bonus-tag".to(bonusTag),
                                "@bonus-venda".to(sellBonus),
                                "@preco".to(shardPrice.suffixed())
                            )
                            .colorMeta()
                            .addReferenceValue(NBTKeys.BAG_SHARD, new NBTTagByte(shard.getId()));
                        return new MenuItem("Fragmento", pair.second(), shardItem, null);
                    })
                    .collect(Collectors.toList());
            });
        }).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent event) {
        if (menuItem.getId().equals("Fragmento")) {
            final byte shardId = menuItem.getItem().getReferenceValue(NBTKeys.BAG_SHARD, NBTTagCompound::getByte);

            if (shardsConfig.data().getById(shardId) == null) {
                player.respond("Fragmento-inexistente");
                return;
            }

            if (event.isRightClick() && mainConfig.data().isShardWithdrawEnabled()) {
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

            if (event.isLeftClick() && mainConfig.data().getShardSellOptions() != null) {
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
        }
    }
}
