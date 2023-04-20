package com.worldplugins.rankup.command.shard;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.GlobalKeys;
import com.worldplugins.rankup.WorldRankup;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.factory.ShardFactory;
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
public class GiveShardLimit implements CommandModule {
    private final @NonNull ConfigCache<ShardsData> shardsConfig;
    private final @NonNull ConfigCache<MainData> mainConfig;
    private final @NonNull ShardFactory shardFactory;
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup darlimite",
        permission = "worldrankup.darlimite",
        argsChecks = {@ArgsChecker(size = 4)},
        usage = "&cArgumentos invalidos. Digite /rankup darlimite <jogador> <fragmento> <quantia> <fisico|virtual>"
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

        final String sendType = args[3];
        final Integer amount = Integer.parseInt(args[2].numerify());

        if (sendType.equals(GlobalKeys.PHYISIC_SEND)) {
            player.giveItems(shardFactory.createLimit(configShard.getId(), amount));
            sender.respond("Limite-fisico-enviado", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia".to(amount.suffixed()),
                "@jogador".to(player.getName())
            ));
            player.respond("Limite-fisico-recebido", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia".to(amount.suffixed())
            ));
            return;
        }

        if (sendType.equals(GlobalKeys.VIRTUAL_SEND)) {
            if (amount > WorldRankup.MAX_COMPARATIVE_SHARD_AMOUNT) {
                sender.respond("Fragmentos-quantia-maxima");
                return;
            }

            playerService.consumePlayer(player.getUniqueId(), playerModel -> {
                final byte shardId = configShard.getId();
                final Integer currentLimit = playerModel.getShardLimit(shardId);
                final Integer newLimit = Math.min(currentLimit + amount, configShard.getLimit());
                final Integer addedAmount = currentLimit + amount != newLimit
                    ? newLimit - currentLimit
                    : amount;

                playerModel.setShardLimit(shardId, newLimit);

                if (addedAmount < amount && mainConfig.data().hasLimitCompensation(ShardCompensation.COMMAND)) {
                    final Integer omittedAmount = amount - addedAmount;
                    player.giveItems(shardFactory.createLimit(shardId, omittedAmount));
                    player.respond("Limite-compensacao", message -> message.replace(
                        "@fragmento".to(configShard.getDisplay()),
                        "@quantia".to(omittedAmount.suffixed())
                    ));
                }

                sender.respond("Limite-virtual-enviado", message -> message.replace(
                    "@fragmento".to(configShard.getDisplay()),
                    "@quantia-adicionada".to(addedAmount.suffixed()),
                    "@jogador".to(player.getName()),
                    "@limite-atual".to(newLimit.suffixed()),
                    "@limite-max".to(((Integer) configShard.getLimit()).suffixed())
                ));
                player.respond("Limite-virtual-recebido", message -> message.replace(
                    "@fragmento".to(configShard.getDisplay()),
                    "@quantia".to(addedAmount.suffixed())
                ));
            });
            return;
        }

        sender.respond("Tipo-envio-invalido", message -> message.replace(
            "@tipo".to(sendType)
        ));
    }
}
