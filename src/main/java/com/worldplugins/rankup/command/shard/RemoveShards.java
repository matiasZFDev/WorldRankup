package com.worldplugins.rankup.command.shard;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class
})

@RequiredArgsConstructor
public class RemoveShards implements CommandModule {
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup removerfragmentos",
        permission = "worldrankup.removerfragmentos",
        argsChecks = {@ArgsChecker(size = 3)},
        usage = "&cArgumentos invalidos. Digite /rankup removerfragmentos <jogador> <fragmento> <quantia>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.respond("Jogador-offline");
            return;
        }

        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getByName(args[1]);

        if (configShard == null) {
            final List<String> existingShards = shardsConfig.get().getAll()
                .stream().map(ShardsConfig.Config.Shard::getName)
                .collect(Collectors.toList());
            sender.respond("Fragmento-inexistente", message -> message.replace(
                "@fragmento".to(args[1]),
                "@existentes".to(existingShards.toString())
            ));
            return;
        }

        if (!args[2].isValidValue()) {
            sender.respond("Quantia-invalida");
            return;
        }

        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final byte shardId = configShard.getId();
            final int amount = Integer.parseInt(args[2].numerify());
            final int playerShards = playerModel.getShards(shardId);
            final Integer removedAmount = Math.min(playerShards, amount);

            playerModel.setShards(shardId, playerShards - removedAmount);
            sender.respond("Fragmentos-removidos", message -> message.replace(
                "@jogador".to(player.getName()),
                "@quantia-removida".to(removedAmount.suffixed()),
                "@quantia-atual".to(((Integer) playerModel.getShards(shardId)).suffixed()),
                "@limite".to(((Integer) playerModel.getShardLimit(shardId)).suffixed()),
                "@fragmento".to(configShard.getDisplay())
            ));
        });
    }
}
