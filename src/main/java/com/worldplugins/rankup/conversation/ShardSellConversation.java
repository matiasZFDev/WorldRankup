package com.worldplugins.rankup.conversation;

import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.view.BagView;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Numbers;
import me.post.lib.view.Views;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class ShardSellConversation extends StringPrompt {
    private final byte shardId;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull Economy economy;

    public ShardSellConversation(
        byte shardId,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull Economy economy
    ) {
        this.shardId = shardId;
        this.playerService = playerService;
        this.mainConfig = mainConfig;
        this.shardsConfig = shardsConfig;
        this.economy = economy;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);

        respond(((Player) context.getForWhom()), "Vender-fragmentos", message -> message.replace(
            to("@fragmento", configShard.display()),
            to("@valor", NumberFormats.suffixed(configShard.price()))
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (value.equalsIgnoreCase("cancelar")) {
            respond(player, "Venda-cancelada");
            Views.get().open(player, BagView.class);
            return null;
        }

        if (mainConfig.data().shardSellOptions() == null) {
            respond(player, "Venda-desabilitada");
            return null;
        }

        if (!NumberFormats.isValidValue(value)) {
            respond(player, "Quantia-invalida");
            return null;
        }

        final Integer amount = Numbers.toIntOrNull(NumberFormats.numerify(value));

        if (amount == null) {
            respond(player, "Quantia-invalida");
            return null;
        }

        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);

        if (configShard == null) {
            respond(player, "Fragmento-invalido");
            return null;
        }

        final @NotNull RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        if (playerModel.getShards(shardId) < amount) {
            respond(player, "Vender-quantia-insuficiente");
            return null;
        }

        final MainData.ShardSellOptions.SellBonus sellBonus = mainConfig.data()
            .shardSellOptions()
            .getBonus(player);
        final Double shardsValue = amount * configShard.price();
        final Double shardsMoney = Numbers.applyPercentage(
            shardsValue,
            sellBonus == null ? 0d : sellBonus.bonus(), Numbers.ApplyType.INCREMENT
        );

        playerModel.setShards(shardId, playerModel.getShards(shardId) - amount);
        economy.depositPlayer(player, shardsMoney);
        respond(player, "Fragmentos-vendidos", message -> message.replace(
            to("@quantia", NumberFormats.suffixed(amount)),
            to("@dinheiro", NumberFormats.suffixed(shardsMoney)),
            to("@fragmento", configShard.display())
        ));
        return null;
    }
}
