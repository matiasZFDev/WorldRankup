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
public class RegressPrestige implements CommandModule {
    private final @NonNull PlayerService playerService;
    private final @NonNull EvolutionManager evolutionManager;
    private final @NonNull PrestigeConfig prestigeConfig;

    @Command(
        name = "rankup regredirprestigio",
        permission = "worldrankup.prestigioregredir",
        argsChecks = {@ArgsChecker(size = 1)},
        usage = "&cArgumentos invalidos. Digite /rankup prestigioregredir <jogador>."
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
            final Prestige previousPrestige = prestigeConfig.get().getPrestiges().getPrevious(configPrestige.getId());

            if (previousPrestige == null) {
                sender.respond("Regredir-prestigio-primeiro");
                return;
            }

            evolutionManager.setPrestige(player, previousPrestige.getId());
            sender.respond("Prestigio-regredido", message -> message.replace(
                "@jogador".to(player.getName()),
                "@rank".to(previousPrestige.getDisplay())
            ));
        });
    }
}
