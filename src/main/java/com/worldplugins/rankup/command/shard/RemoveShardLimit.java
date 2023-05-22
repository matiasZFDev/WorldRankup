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

public class RemoveShardLimit implements CommandModule {
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull PlayerService playerService;

    public RemoveShardLimit(@NotNull ConfigModel<ShardsData> shardsConfig, @NotNull PlayerService playerService) {
        this.shardsConfig = shardsConfig;
        this.playerService = playerService;
    }

    //usage = "&cArgumentos invalidos. Digite /rankup removerlimite <jogador> <fragmento> <quantia>"
    @Command(name = "rankup removerlimite")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.removerlimite")) {
            respond(sender, "Remover-limite-permissoes");
            return;
        }

        if (args.length != 3) {
            respond(sender, "Remover-limite-uso");
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
            final int playerLimit = playerModel.getShardLimit(shardId);
            final int removedAmount = Math.min(playerLimit, amount);

            playerModel.setShardLimit(shardId, playerLimit - removedAmount);
            respond(sender, "Limite-removido", message -> message.replace(
                to("@jogador", player.getName()),
                to("@quantia-removida", NumberFormats.suffixed(removedAmount)),
                to("@fragmento", configShard.display()),
                to("@limite-atual", NumberFormats.suffixed(playerModel.getShardLimit(shardId))),
                to("@limite-max", NumberFormats.suffixed(configShard.limit()))
            ));
        });
    }
}
