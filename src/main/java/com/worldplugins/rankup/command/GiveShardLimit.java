package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.GlobalKeys;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.config.data.ShardCompensation;
import com.worldplugins.rankup.database.model.RankupPlayer;
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
import java.util.Optional;
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
    private final @NonNull ShardsConfig shardsConfig;
    private final @NonNull MainConfig mainConfig;
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

        final String sendType = args[3];
        final Integer amount = Integer.parseInt(args[2].numerify());

        if (sendType.equals(GlobalKeys.PHYISIC_SEND)) {
            player.giveItems(shardFactory.createLimit(configShard.get().getId(), amount));
            sender.respond("Limite-fisico-enviado", message -> message.replace(
                "@fragmento".to(configShard.get().getDisplay()),
                "@quantia".to(amount.suffixed()),
                "@jogador".to(player.getName())
            ));
            player.respond("Limite-fisico-recebido", message -> message.replace(
                "@fragmento".to(configShard.get().getDisplay()),
                "@quantia".to(amount.suffixed())
            ));
            return;
        }

        if (sendType.equals(GlobalKeys.VIRTUAL_SEND)) {
            final byte shardId = configShard.get().getId();
            final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
            final Integer currentLimit = playerModel.getShardLimit(shardId);
            final Integer newLimit = Math.min(currentLimit + amount, configShard.get().getLimit());
            final Integer addedAmount = currentLimit + amount != newLimit
                ? newLimit - currentLimit
                : amount;

            playerModel.setShardLimit(shardId, newLimit);
            playerService.update(playerModel);

            if (addedAmount < amount && mainConfig.get().hasShardCompensation(ShardCompensation.COMMAND)) {
                final Integer omittedAmount = amount - addedAmount;
                player.giveItems(shardFactory.createLimit(shardId, omittedAmount));
                player.respond("Limite-compensacao", message -> message.replace(
                    "@fragmento".to(configShard.get().getDisplay()),
                    "@quantia".to(omittedAmount.suffixed())
                ));
            }

            sender.respond("Limite-virtual-enviado", message -> message.replace(
                "@fragmento".to(configShard.get().getDisplay()),
                "@quantia-adicionada".to(addedAmount.suffixed()),
                "@jogador".to(player.getName()),
                "@limite-atual".to(newLimit.suffixed()),
                "@limite-max".to(((Integer) configShard.get().getLimit()).suffixed())
            ));
            player.respond("Limite-virtual-recebido", message -> message.replace(
                "@fragmento".to(configShard.get().getDisplay()),
                "@quantia".to(addedAmount.suffixed())
            ));
            return;
        }

        sender.respond("Tipo-envio-invalido", message -> message.replace(
            "@tipo".to(sendType)
        ));
    }
}
