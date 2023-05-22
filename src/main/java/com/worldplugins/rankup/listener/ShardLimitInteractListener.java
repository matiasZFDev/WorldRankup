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
import me.post.lib.util.Inventories;
import me.post.lib.util.NBTs;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Players;
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

public class ShardLimitInteractListener implements Listener {
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;
    private final @NotNull ShardFactory shardFactory;

    public ShardLimitInteractListener(
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

        if (!NBTs.hasTag(event.getItem(), NBTKeys.PHYISIC_LIMIT_ID)) {
            return;
        }

        event.setCancelled(true);

        final byte shardId = NBTs.getTagValue(
            event.getItem(),
            NBTKeys.PHYISIC_LIMIT_ID,
            NBTCompound::getByte
        );
        final Integer amount = NBTs.getTagValue(
            event.getItem(),
            NBTKeys.PHYISIC_LIMIT_AMOUNT,
            NBTCompound::getInteger
        );

        final Player player = event.getPlayer();
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);

        if (configShard == null) {
            respond(player, "Limite-invalido");
            return;
        }

        if (player.isSneaking()) {
            final List<Pair<ItemStack, Integer>> inventoryLimit = Inventories.allWithReference(
                player.getInventory(), NBTKeys.PHYISIC_LIMIT_ID
            );

            if (inventoryLimit.size() == 1 && inventoryLimit.get(0).first().getAmount() == 1) {
                respond(player, "Limite-juntar-nada");
                return;
            }

            final Integer mergedAmount = inventoryLimit
                .stream()
                .mapToInt(item ->
                    NBTs.getTagValue(
                        item.first(),
                        NBTKeys.PHYISIC_LIMIT_AMOUNT,
                        NBTCompound::getInteger
                    ) * item.first().getAmount()
                )
                .sum();

            if (mergedAmount >= WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT) {
                respond(player, "Limite-juntar-max", message -> message.replace(
                    to("@quantia-max", NumberFormats.suffixed(WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT))
                ));
                return;
            }

            Inventories.removeWithReference(player.getInventory(), NBTKeys.PHYISIC_LIMIT_ID, true);
            Players.giveItems(player, shardFactory.createLimit(shardId, mergedAmount));
            respond(player, "Limite-junto", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia", NumberFormats.suffixed(mergedAmount))
            ));
            return;
        }

        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        if (playerModel == null)
            return;

        if (playerModel.getShardLimit(shardId) == configShard.limit()) {
            respond(player, "Ativar-limite-maximo");
            return;
        }

        final int playerLimit = playerModel.getShardLimit(shardId);
        final int limitSum = playerLimit + amount;
        final int setAmount = Math.min(limitSum, configShard.limit());
        final Integer addedAmount = limitSum > configShard.limit()
            ? configShard.limit() - playerLimit
            : amount;

        if (limitSum > configShard.limit()) {
            Players.giveItems(player, shardFactory.createLimit(shardId, limitSum - configShard.limit()));
        }

        playerModel.setShardLimit(shardId, setAmount);
        Players.reduceHandItem(player);
        respond(player, "Limite-ativado", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@quantia", NumberFormats.suffixed(addedAmount))
        ));
    }
}
