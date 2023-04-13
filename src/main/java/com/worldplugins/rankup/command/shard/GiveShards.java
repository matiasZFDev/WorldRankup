package com.worldplugins.rankup.command.shard;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.GlobalKeys;
import com.worldplugins.rankup.WorldRankup;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.config.data.ShardCompensation;
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
public class GiveShards implements CommandModule {
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull MainConfig mainConfig;
    private final @NonNull ShardFactory shardFactory;
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup darfragmentos",
        permission = "worldrankup.darfragmentos",
        argsChecks = {@ArgsChecker(size = 4)},
        usage = "&cArgumentos invalidos. Digite /rankup darfragmentos <jogador> <fragmento> <quantia> <fisico|virtual>"
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

        final String sendType = args[3];
        final Integer amount = Integer.parseInt(args[2].numerify());

        if (sendType.equals(GlobalKeys.PHYISIC_SEND)) {
            player.giveItems(shardFactory.createShard(configShard.getId(), amount));
            sender.respond("Fragmentos-fisicos-enviados", message -> message.replace(
                "@fragmento".to(configShard.getDisplay()),
                "@quantia".to(amount.suffixed()),
                "@jogador".to(player.getName())
            ));
            player.respond("Fragmentos-fisicos-recebidos", message -> message.replace(
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
                final int currentAmount = playerModel.getShards(shardId);
                final Integer setAmount = playerModel.setShards(shardId, playerModel.getShards(shardId) + amount);
                final Integer shardLimit = playerModel.getShardLimit(shardId);
                final Integer addedAmount = currentAmount + amount > setAmount
                    ? setAmount - currentAmount
                    : amount;

                if (addedAmount < amount && mainConfig.get().hasShardCompensation(ShardCompensation.COMMAND)) {
                    final Integer omittedAmount = amount - addedAmount;
                    player.giveItems(shardFactory.createShard(shardId, omittedAmount));
                    player.respond("Fragmentos-compensacao", message -> message.replace(
                        "@fragmento".to(configShard.getDisplay()),
                        "@quantia".to(omittedAmount.suffixed())
                    ));
                }

                sender.respond("Fragmentos-virtuais-enviados", message -> message.replace(
                    "@fragmento".to(configShard.getDisplay()),
                    "@quantia-adicionada".to(addedAmount.suffixed()),
                    "@jogador".to(player.getName()),
                    "@quantia-atual".to(setAmount.suffixed()),
                    "@limite".to(shardLimit.suffixed())
                ));
                player.respond("Fragmentos-virtuais-recebidos", message -> message.replace(
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
