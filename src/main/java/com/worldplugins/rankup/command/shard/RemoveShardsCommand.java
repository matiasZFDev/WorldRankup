package com.worldplugins.rankup.command.shard;

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

public class RemoveShardsCommand implements CommandModule {
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;

    public RemoveShardsCommand(@NotNull ConfigModel<ShardsData> shardsConfig, @NotNull PlayerService playerService) {
        this.shardsConfig = shardsConfig;
        this.playerService = playerService;
    }

    // usage = "&cArgumentos invalidos. Digite /rankup removerfragmentos <jogador> <fragmento> <quantia>"
    @Command(name = "rankup removerfragmentos")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.removerfragmentos")) {
            respond(sender, "Remover-fragmentos-permissoes");
            return;
        }

        if (args.length != 3) {
            respond(sender, "Remover-fragmentos-uso");
            return;
        }

        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            respond(sender, "Jogador-offline");
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

        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final byte shardId = configShard.id();
            final int amount = Integer.parseInt(NumberFormats.numerify(args[2]));
            final int playerShards = playerModel.getShards(shardId);
            final Integer removedAmount = Math.min(playerShards, amount);

            playerModel.setShards(shardId, playerShards - removedAmount);
            respond(sender, "Fragmentos-removidos", message -> message.replace(
                to("@jogador", player.getName()),
                to("@quantia-removida", NumberFormats.suffixed(removedAmount)),
                to("@quantia-atual", NumberFormats.suffixed(playerModel.getShards(shardId))),
                to("@limite", NumberFormats.suffixed(playerModel.getShardLimit(shardId))),
                to("@fragmento", configShard.display())
            ));
        });
    }
}
