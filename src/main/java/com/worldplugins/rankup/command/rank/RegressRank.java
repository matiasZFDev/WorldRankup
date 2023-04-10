package com.worldplugins.rankup.command.rank;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.rankup.config.RanksConfig;
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
    private final @NonNull RanksConfig ranksConfig;

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
            final RanksConfig.Config.Rank configRank = ranksConfig.get().getById(playerModel.getRank());
            final RanksConfig.Config.Rank previousRank = ranksConfig.get().getPrevious(configRank.getId());

            if (previousRank == null) {
                sender.respond("Regredir-rank-primeiro");
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
