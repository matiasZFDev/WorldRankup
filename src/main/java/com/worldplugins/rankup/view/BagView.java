package com.worldplugins.rankup.view;

import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewOf;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.config.menu.BagMenuContainer;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
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
    ResponseExtensions.class
})

@ViewOf(menuContainer = BagMenuContainer.class)
@RequiredArgsConstructor
public class BagView extends MenuDataView<ViewContext> {
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull PlayerService playerService;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final List<Integer> itemSlots = menuData.getData("Slots");
        final ItemStack shardItemModel = menuData.getData("Iten-fragmento");
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            session.addDynamics(() -> {
                return shardsConfig.get().getAll().zip(itemSlots).stream()
                    .map(pair -> {
                        final ShardsConfig.Config.Shard shard = pair.first();
                        final ItemStack shardItem = shard.getItem()
                            .display(shardItemModel)
                            .nameFormat("@nome".to(shard.getDisplay()))
                            .loreFormat(
                                "@quantia".to(((Integer) playerModel.getShards(shard.getId())).suffixed()),
                                "@limite".to(((Integer) playerModel.getShardLimit(shard.getId())).suffixed()),
                                "@limite-max".to(((Integer) shard.getLimit()).suffixed()),
                                "@preco".to(((Double) shard.getPrice()).suffixed())
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

            if (!shardsConfig.get().hasShard(shardId)) {
                player.respond("Fragmento-inexistente");
                return;
            }
        }
    }
}
