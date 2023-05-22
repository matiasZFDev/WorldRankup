package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.WorldRankup;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.ShardFactory;
import me.post.deps.nbt_api.nbtapi.NBTCompound;
import me.post.lib.common.Pair;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class ShardInteractListener implements Listener {
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;
    private final @NotNull ShardFactory shardFactory;

    public ShardInteractListener(
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull PlayerService playerService,
        @NotNull ShardFactory shardFactory
    ) {
        this.shardsConfig = shardsConfig;
        this.playerService = playerService;
        this.shardFactory = shardFactory;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }


        if (!NBTs.hasTag(event.getItem(), NBTKeys.PHYISIC_SHARD_ID)) {
            return;
        }

        event.setCancelled(true);

        final byte shardId = NBTs.getTagValue(event.getItem(), NBTKeys.PHYISIC_SHARD_ID, NBTCompound::getByte);
        final Player player = event.getPlayer();
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);

        if (configShard == null) {
            respond(player, "Fragmento-invalido");
            return;
        }

        if (player.isSneaking()) {
            final List<Pair<ItemStack, Integer>> inventoryShards = Inventories.allWithReference(
                player.getInventory(),
                NBTKeys.PHYISIC_SHARD_ID
            );

            if (inventoryShards.size() == 1 && inventoryShards.get(0).first().getAmount() == 1) {
                respond(player, "Fragmentos-juntar-nada");
                return;
            }

            final int mergedAmount = inventoryShards
                .stream()
                .mapToInt(item ->
                    NBTs.getTagValue(
                        item.first(),
                        NBTKeys.PHYISIC_SHARD_AMOUNT,
                        NBTCompound::getInteger
                    ) * item.first().getAmount()
                )
                .sum();

            if (mergedAmount > WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT) {
                respond(player, "Fragmentos-juntar-max", message -> message.replace(
                    to("@quantia-max", NumberFormats.suffixed(WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT))
                ));
                return;
            }

            Inventories.removeWithReference(player.getInventory(), NBTKeys.PHYISIC_SHARD_ID, true);
            Players.giveItems(player, shardFactory.createShard(shardId, mergedAmount));
            respond(player, "Fragmentos-juntados", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia", NumberFormats.suffixed(mergedAmount))
            ));
            return;
        }

        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        if (playerModel == null) {
            return;
        }

        final Integer amount = NBTs.getTagValue(
            event.getItem(),
            NBTKeys.PHYISIC_SHARD_AMOUNT,
            NBTCompound::getInteger
        );
        final int playerShards = playerModel.getShards(shardId);
        final int playerShardLimit = playerModel.getShardLimit(shardId);

        if (playerShards == playerShardLimit) {
            respond(player, "Ativar-fragmento-limite");
            return;
        }

        final Integer addedAmount = playerShards + amount > playerShardLimit
            ? playerShardLimit - playerShards
            : amount;

        if (!addedAmount.equals(amount)) {
            Players.giveItems(player, shardFactory.createShard(shardId, amount - addedAmount));
        }

        playerModel.setShards(shardId, playerShards + addedAmount);
        Players.reduceHandItem(player);
        respond(player, "Fragmento-ativado", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@quantia", NumberFormats.suffixed(addedAmount))
        ));
    }
}
