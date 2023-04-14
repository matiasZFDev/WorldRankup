package com.worldplugins.rankup.command.prestige;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.rankup.config.PrestigeConfig;
import com.worldplugins.rankup.config.data.prestige.Prestige;
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
public class EvolvePrestige implements CommandModule {
    private final @NonNull PlayerService playerService;
    private final @NonNull EvolutionManager evolutionManager;
    private final @NonNull PrestigeConfig prestigeConfig;

    @Command(
        name = "rankup evoluirprestigio",
        permission = "worldrankup.prestigioevoluir",
        argsChecks = {@ArgsChecker(size = 1)},
        usage = "&cArgumentos invalidos. Digite /rankup evoluirprestigio <jogador>."
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
            final Prestige configPrestige = prestigeConfig.get().getPrestiges().getById(playerModel.getPrestige());

            if (configPrestige.getNext() == null) {
                sender.respond("Evoluir-prestigio-ultimo", message -> message.replace(
                    "@jogador".to(player.getName())
                ));
                return;
            }

            final Prestige nextPrestige = prestigeConfig.get().getPrestiges().getById(
                configPrestige.getNext()
            );
            evolutionManager.setPrestige(player, nextPrestige.getId());
            sender.respond("Prestigio-evoluido-comando", message -> message.replace(
                "@jogador".to(player.getName()),
                "@prestigio".to(nextPrestige.getDisplay())
            ));
        });
    }
}
