package com.worldplugins.rankup.conversation;

import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.ShardFactory;
import com.worldplugins.rankup.view.BagView;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Numbers;
import me.post.lib.util.Players;
import me.post.lib.view.Views;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class ShardWithdrawConversation extends StringPrompt {
    private final byte shardId;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull ShardFactory shardFactory;

    public ShardWithdrawConversation(
        byte shardId,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull ShardFactory shardFactory
    ) {
        this.shardId = shardId;
        this.playerService = playerService;
        this.mainConfig = mainConfig;
        this.shardsConfig = shardsConfig;
        this.shardFactory = shardFactory;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);

        respond(((Player) context.getForWhom()), "Retirar-fragmentos", message -> message.replace(
            to("@fragmento", configShard.display())
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (value.equalsIgnoreCase("cancelar")) {
            respond(player, "Retiro-cancelado");
            Views.get().open(player, BagView.class);
            return null;
        }

        if (!mainConfig.data().isShardWithdrawEnabled()) {
            respond(player, "Retiro-desabilitado");
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
            respond(player, "Retirar-quantia-insuficiente");
            return null;
        }

        playerModel.setShards(shardId, playerModel.getShards(shardId) - amount);
        Players.giveItems(player, shardFactory.createShard(shardId, amount));
        respond(player, "Fragmentos-retirados", message -> message.replace(
            to("@quantia", NumberFormats.suffixed(amount)),
            to("@fragmento", configShard.display())
        ));
        return null;
    }
}
