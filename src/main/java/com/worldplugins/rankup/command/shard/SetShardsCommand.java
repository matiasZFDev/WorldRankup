package com.worldplugins.rankup.command.shard;

import com.worldplugins.rankup.WorldRankup;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.database.service.PlayerService;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class SetShardsCommand implements CommandModule {
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;

    public SetShardsCommand(@NotNull ConfigModel<ShardsData> shardsConfig, @NotNull PlayerService playerService) {
        this.shardsConfig = shardsConfig;
        this.playerService = playerService;
    }

    // usage = "&cArgumentos invalidos. Digite /rankup setarfragmentos <jogador> <fragmento> <quantia>"
    @Command(name = "rankup setarfragmentos")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.setarfragmentos")) {
            respond(sender, "Setar-fragmentos-permissoes");
            return;
        }

        if (args.length != 3) {
            respond(sender, "Setar-fragmentos-uso");
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

        final int amount = Integer.parseInt(NumberFormats.numerify(args[2]));

        if (amount > WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT) {
            respond(sender, "Fragmentos-quantia-maxima");
            return;
        }

        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final byte shardId = configShard.id();
            final Integer finalAmount = playerModel.setShards(shardId, amount);
            final Integer shardLimit = playerModel.getShardLimit(shardId);
            final Integer currentAmount = playerModel.getShards(shardId);

            respond(sender, "Fragmentos-setados", message -> message.replace(
                to("@fragmento", configShard.display()),
                to("@quantia-setada", NumberFormats.suffixed(finalAmount)),
                to("@jogador", player.getName()),
                to("@quantia-atual", NumberFormats.suffixed(currentAmount)),
                to("@limite", NumberFormats.suffixed(shardLimit))
            ));
        });
    }
}
