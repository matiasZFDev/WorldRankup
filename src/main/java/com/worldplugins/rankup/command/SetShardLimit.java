package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
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
    NumberFormatExtensions.class,
    ItemExtensions.class,
    PlayerExtensions.class
})

@RequiredArgsConstructor
public class SetShardLimit implements CommandModule {
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup setarlimite",
        permission = "worldrankup.setarlimite",
        argsChecks = {@ArgsChecker(size = 3)},
        usage = "&cArgumentos invalidos. Digite /rankup setarlimite <jogador> <fragmento> <quantia>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.respond("Jogador-offline", message -> message.replace("@jogador".to(args[0])));
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
            final int amount = Integer.parseInt(args[2].numerify());
            final byte shardId = configShard.getId();
            final Integer finalAmount = Math.min(amount, configShard.getLimit());

            playerModel.setShardLimit(shardId, finalAmount);
            sender.respond("Limite-setado", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia-setada".to(finalAmount.suffixed()),
                "@jogador".to(player.getName()),
                "@limite-max".to(((Integer) configShard.getLimit()).suffixed())
            ));
        });
    }
}
