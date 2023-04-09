package com.worldplugins.rankup.command.rank;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.rankup.config.RanksConfig;
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
public class SetRank implements CommandModule {
    private final @NonNull EvolutionManager evolutionManager;
    private final @NonNull RanksConfig ranksConfig;

    @Command(
        name = "rankup setrank",
        permission = "worldrankup.setrank",
        argsChecks = {@ArgsChecker(size = 2)},
        usage = "&cArgumentos invalidos. Digite /rankup setrank <jogador> <rank>."
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

        final RanksConfig.Config.Rank configRank = ranksConfig.get().getByName(args[1]);

        if (configRank == null) {
            sender.respond("Setar-rank-invalido", message -> message.replace(
                "@rank".to(args[1])
            ));
            return;
        }

        evolutionManager.setRank(player, configRank.getId());
        sender.respond("Rank-setado", message -> message.replace(
            "@jogador".to(player.getName()),
            "@rank".to(configRank.getDisplay())
        ));
    }
}
