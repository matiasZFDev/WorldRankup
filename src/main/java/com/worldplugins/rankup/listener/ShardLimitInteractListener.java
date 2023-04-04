package com.worldplugins.rankup.listener;

import com.worldplugins.lib.common.SlotItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.InventoryExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.factory.ShardFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

@ExtensionMethod({
    NBTExtensions.class,
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    PlayerExtensions.class,
    InventoryExtensions.class
})

@RequiredArgsConstructor
public class ShardLimitInteractListener implements Listener {
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull PlayerService playerService;
    private final @NonNull ShardFactory shardFactory;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem())
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!event.getItem().hasReference(NBTKeys.PHYISIC_LIMIT_ID))
            return;

        final byte shardId = event.getItem().getReferenceValue(
            NBTKeys.PHYISIC_LIMIT_ID,
            NBTTagCompound::getByte
        );
        final Integer amount = event.getItem().getReferenceValue(
            NBTKeys.PHYISIC_LIMIT_AMOUNT,
            NBTTagCompound::getInt
        );

        final Player player = event.getPlayer();

        if (!shardsConfig.get().hasShard(shardId)) {
            player.respond("Limite-invalido");
            return;
        }

        if (player.isSneaking()) {
            final List<SlotItem> inventoryLimit = player.getInventory().allWithReference(NBTKeys.PHYISIC_LIMIT_ID);

            if (inventoryLimit.size() == 1 && inventoryLimit.get(0).item().getAmount() == 1) {
                player.respond("Limite-juntar-nada");
                return;
            }

            final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);
            final Integer mergedAmount = inventoryLimit
                .stream()
                .mapToInt(item ->
                    item.item().getReferenceValue(
                        NBTKeys.PHYISIC_LIMIT_AMOUNT, NBTTagCompound::getInt
                    ) * item.item().getAmount()
                )
                .sum();

            player.getInventory().removeWithReference(NBTKeys.PHYISIC_LIMIT_ID, true);
            player.giveItems(shardFactory.createLimit(shardId, mergedAmount));
            player.respond("Limite-junto", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia".to(mergedAmount.suffixed())
            ));
            return;
        }

        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);

        if (playerModel.getShardLimit(shardId) == configShard.getLimit()) {
            player.respond("Ativar-limite-maximo");
            return;
        }

        final int limitSum = playerModel.getShardLimit(shardId) + amount;
        final int setAmount = Math.min(limitSum, configShard.getLimit());
        final Integer addedAmount = limitSum > configShard.getLimit()
            ? limitSum - configShard.getLimit()
            : amount;

        if (limitSum > configShard.getLimit()) {
            player.giveItems(shardFactory.createShard(shardId, limitSum - configShard.getLimit()));
        }

        playerModel.setShardLimit(shardId, setAmount);
        playerService.update(playerModel);
        player.reduceHandItem();
        player.respond("Limite-ativado", message -> message.replace(
            "@fragmento".to(configShard.getDisplay()),
            "@quantia".to(addedAmount.suffixed())
        ));
    }
}
