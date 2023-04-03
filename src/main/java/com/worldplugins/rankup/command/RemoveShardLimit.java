package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class
})

@RequiredArgsConstructor
public class RemoveShardLimit implements CommandModule {
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup removerlimite",
        permission = "worldrankup.removerlimite",
        argsChecks = {@ArgsChecker(size = 3)},
        usage = "&cArgumentos invalidos. Digite /rankup removerlimite <jogador> <fragmento> <quantia>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.respond("Jogador-offline");
            return;
        }

        final Optional<ShardsConfig.Config.Shard> configShard = shardsConfig.get().getByName(args[1]);

        if (!configShard.isPresent()) {
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

        final byte shardId = configShard.get().getId();
        final int amount = Integer.parseInt(args[2].numerify());
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
        final int playerLimit = playerModel.getShardLimit(shardId);
        final Integer removedAmount = Math.min(playerLimit, amount);

        playerModel.setShardLimit(shardId, playerLimit - removedAmount);
        sender.respond("Limite-removido", message -> message.replace(
            "@jogador".to(player.getName()),
            "@quantia-removida".to(removedAmount.suffixed()),
            "@fragmento".to(configShard.get().getDisplay()),
            "@limite-atual".to(((Integer) playerModel.getShardLimit(shardId)).suffixed()),
            "@limite-max".to(((Integer) configShard.get().getLimit()).suffixed())
        ));
    }
}
