package com.worldplugins.rankup.command.shard;

import com.worldplugins.rankup.GlobalKeys;
import com.worldplugins.rankup.WorldRankup;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.ShardFactory;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class GiveShardsCommand implements CommandModule {
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ShardFactory shardFactory;
    private final @NotNull PlayerService playerService;

    public GiveShardsCommand(
        @NotNull ConfigModel<ShardsData> shardsConfig,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ShardFactory shardFactory,
        @NotNull PlayerService playerService
    ) {
        this.shardsConfig = shardsConfig;
        this.mainConfig = mainConfig;
        this.shardFactory = shardFactory;
        this.playerService = playerService;
    }

    @Command(name = "rankup darfragmentos")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.darfragmentos")) {
            respond(sender, "Dar-fragmentos-permissoes");
            return;
        }

        if (args.length != 4) {
            respond(sender, "Dar-fragmentos-uso");
            return;
        }

        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            respond(sender, "Jogador-offline", message -> message.replace(to("@jogador", args[0])));
            return;
        }

        final ShardsData.Shard configShard = shardsConfig.data().getByName(args[1]);

        if (configShard == null) {
            final List<String> existingShards = shardsConfig.data().getAll()
                    .stream().map(ShardsData.Shard::name)
                    .collect(Collectors.toList());
            respond(sender, "Fragmento-inexistente", message -> message.replace(
                to("@fragmento", args[1]),
                to("@existentes", existingShards.toString())
            ));
            return;
        }

        if (!NumberFormats.isValidValue(args[2])) {
            respond(sender, "Quantia-invalida");
            return;
        }

        final String sendType = args[3];
        final int amount = Integer.parseInt(NumberFormats.numerify(args[2]));

        if (sendType.equals(GlobalKeys.PHYISIC_SEND)) {
            Players.giveItems(player, shardFactory.createShard(configShard.id(), amount));
            respond(sender, "Fragmentos-fisicos-enviados", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia", NumberFormats.suffixed(amount)),
                to("@jogador", player.getName())
            ));
            respond(player, "Fragmentos-fisicos-recebidos", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia", NumberFormats.suffixed(amount))
            ));
            return;
        }

        if (sendType.equals(GlobalKeys.VIRTUAL_SEND)) {
            if (amount > WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT) {
                respond(sender, "Fragmentos-quantia-maxima");
                return;
            }

            playerService.consumePlayer(player.getUniqueId(), playerModel -> {
                final byte shardId = configShard.id();
                final int currentAmount = playerModel.getShards(shardId);
                final int setAmount = playerModel.setShards(shardId, playerModel.getShards(shardId) + amount);
                final int shardLimit = playerModel.getShardLimit(shardId);
                final int addedAmount = currentAmount + amount > setAmount
                    ? setAmount - currentAmount
                    : amount;

                if (addedAmount < amount && mainConfig.data().hasShardCompensation(ShardCompensation.COMMAND)) {
                    final int omittedAmount = amount - addedAmount;
                    Players.giveItems(player, shardFactory.createShard(shardId, omittedAmount));
                    respond(player, "Fragmentos-compensacao", message -> message.replace(
                        to("@fragmento", configShard.display()),
                        to("@quantia", NumberFormats.suffixed(omittedAmount))
                    ));
                }

                respond(sender, "Fragmentos-virtuais-enviados", message -> message.replace(
                    to("@fragmento", configShard.display()),
                    to("@quantia-adicionada", NumberFormats.suffixed(addedAmount)),
                    to("@jogador", player.getName()),
                    to("@quantia-atual", NumberFormats.suffixed(setAmount)),
                    to("@limite", NumberFormats.suffixed(shardLimit))
                ));
                respond(player, "Fragmentos-virtuais-recebidos", message -> message.replace(
                    to("@fragmento", configShard.display()),
                    to("@quantia", NumberFormats.suffixed(addedAmount))
                ));
            });
            return;
        }

        respond(sender, "Tipo-envio-invalido", message -> message.replace(
            to("@tipo", sendType)
        ));
    }
}
