package com.worldplugins.rankup.conversation;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.extension.ViewExtensions;
import com.worldplugins.rankup.view.BagView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    NumberFormatExtensions.class,
    GenericExtensions.class,
    ViewExtensions.class,
    NumberExtensions.class
})

@RequiredArgsConstructor
public class ShardSellConversation extends StringPrompt {
    private final byte shardId;
    private final @NonNull PlayerService playerService;
    private final @NonNull MainConfig mainConfig;
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull Economy economy;

    @Override
    public String getPromptText(ConversationContext context) {
        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);

        ((Player) context.getForWhom()).respond("Vender-fragmentos", message -> message.replace(
            "@fragmento".to(configShard.getDisplay()),
            "@valor".to(((Double) configShard.getPrice()).suffixed())
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (value.equalsIgnoreCase("cancelar")) {
            player.respond("Venda-cancelada");
            player.openView(BagView.class);
            return null;
        }

        if (mainConfig.get().getShardSellOptions() == null) {
            player.respond("Venda-desabilitada");
            return null;
        }

        if (!value.isValidValue()) {
            player.respond("Quantia-invalida");
            return null;
        }

        final Integer amount = value.numerify().toIntOrNull();

        if (amount == null) {
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
            player.respond("Vender-quantia-insuficiente");
            return null;
        }

        final MainConfig.Config.ShardSellOptions.SellBonus sellBonus = mainConfig.get()
            .getShardSellOptions()
            .getBonus(player);
        final Double shardsValue = amount * configShard.getPrice();
        final Double shardsMoney = shardsValue.applyPercentage(
            sellBonus == null ? 0d : sellBonus.getBonus(), NumberExtensions.ApplyType.INCREMENT
        );

        playerModel.setShards(shardId, playerModel.getShards(shardId) - amount);
        economy.depositPlayer(player, shardsMoney);
        player.respond("Fragmentos-vendidos", message -> message.replace(
            "@quantia".to(amount.suffixed()),
            "@dinheiro".to(shardsMoney.suffixed()),
            "@fragmento".to(configShard.getDisplay())
        ));
        return null;
    }
}
