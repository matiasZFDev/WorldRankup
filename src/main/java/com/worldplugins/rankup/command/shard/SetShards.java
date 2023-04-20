package com.worldplugins.rankup.command.shard;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.WorldRankup;
import com.worldplugins.rankup.config.data.ShardsData;
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
public class SetShards implements CommandModule {
    private final @NonNull ConfigCache<ShardsData> shardsConfig;
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup setarfragmentos",
        permission = "worldrankup.setarfragmentos",
        argsChecks = {@ArgsChecker(size = 3)},
        usage = "&cArgumentos invalidos. Digite /rankup setarfragmentos <jogador> <fragmento> <quantia>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.respond("Jogador-offline", message -> message.replace("@jogador".to(args[0])));
            return;
        }

        final ShardsData.Shard configShard = shardsConfig.data().getByName(args[1]);

        if (configShard == null) {
            final List<String> existingShards = shardsConfig.data().getAll()
                    .stream().map(ShardsData.Shard::getName)
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

        final int amount = Integer.parseInt(args[2].numerify());

        if (amount > WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT) {
            sender.respond("Fragmentos-quantia-maxima");
            return;
        }

        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final byte shardId = configShard.getId();
            final Integer finalAmount = playerModel.setShards(shardId, amount);
            final Integer shardLimit = playerModel.getShardLimit(shardId);
            final Integer currentAmount = playerModel.getShards(shardId);

            sender.respond("Fragmentos-setados", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia-setada".to(finalAmount.suffixed()),
                "@jogador".to(player.getName()),
                "@quantia-atual".to(currentAmount.suffixed()),
                "@limite".to(shardLimit.suffixed())
            ));
        });
    }
}
