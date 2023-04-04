package com.worldplugins.rankup.listener;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
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

@ExtensionMethod({
    NBTExtensions.class,
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    PlayerExtensions.class
})

@RequiredArgsConstructor
public class ShardLimitConsumeListener implements Listener {
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
