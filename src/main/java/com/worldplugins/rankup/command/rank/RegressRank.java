package com.worldplugins.rankup.command.rank;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.manager.EvolutionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class RegressRank implements CommandModule {
    private final @NonNull PlayerService playerService;
    private final @NonNull EvolutionManager evolutionManager;
    private final @NonNull ConfigCache<RanksData> ranksConfig;

    @Command(
        name = "rankup regredir",
        permission = "worldrankup.rankregredir",
        argsChecks = {@ArgsChecker(size = 1)},
        usage = "&cArgumentos invalidos. Digite /rankup regredir <jogador>."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.respond("Jogador-offline", message -> message.replace(
                "@jogador".to(args[0])
            ));
            return;
        }

        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.getRank());
            final RanksData.Rank previousRank = ranksConfig.data().getPrevious(configRank.getId());

            if (previousRank == null) {
                sender.respond("Regredir-rank-primeiro", message -> message.replace(
                    "@jogador".to(player.getName())
                ));
                return;
            }

            evolutionManager.setRank(player, previousRank.getId());
            sender.respond("Rank-regredido", message -> message.replace(
                "@jogador".to(player.getName()),
                "@rank".to(previousRank.getDisplay())
            ));
        });
    }
}
