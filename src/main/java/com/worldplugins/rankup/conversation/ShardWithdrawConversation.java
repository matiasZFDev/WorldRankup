package com.worldplugins.rankup.conversation;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.extension.ViewExtensions;
import com.worldplugins.rankup.factory.ShardFactory;
import com.worldplugins.rankup.view.BagView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    NumberFormatExtensions.class,
    GenericExtensions.class,
    PlayerExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
public class ShardWithdrawConversation extends StringPrompt {
    private final byte shardId;
    private final @NonNull PlayerService playerService;
    private final @NonNull MainConfig mainConfig;
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull ShardFactory shardFactory;

    @Override
    public String getPromptText(ConversationContext context) {
        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);

        ((Player) context.getForWhom()).respond("Retirar-fragmentos", message -> message.replace(
            "@fragmento".to(configShard.getDisplay())
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (value.equalsIgnoreCase("cancelar")) {
            player.respond("Retiro-cancelado");
            player.openView(BagView.class);
            return null;
        }

        if (!mainConfig.get().isShardWithdrawEnabled()) {
            player.respond("Retiro-desabilitado");
            return null;
        }

        if (!value.isValidValue()) {
            player.respond("Quantia-invalida");
            return null;
        }

        final Integer amount = NumberUtils.toInt(value.numerify(), -1);

        if (amount == -1) {
            player.respond("Quantia-invalida");
            return null;
        }

        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);

        if (configShard == null) {
            player.respond("Fragmento-invalido");
            return null;
        }

        final @NonNull RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        if (playerModel.getShards(shardId) < amount) {
            player.respond("Retirar-quantia-insuficiente");
            return null;
        }

        playerModel.setShards(shardId, playerModel.getShards(shardId) - amount);
        player.giveItems(shardFactory.createShard(shardId, amount));
        player.respond("Fragmentos-retirados", message -> message.replace(
            "@quantia".to(amount.suffixed()),
            "@fragmento".to(configShard.getDisplay())
        ));
        return null;
    }
}
