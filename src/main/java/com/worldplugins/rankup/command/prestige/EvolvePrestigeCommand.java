package com.worldplugins.rankup.command.prestige;

import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.manager.EvolutionManager;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class EvolvePrestigeCommand implements CommandModule {
    private final @NotNull PlayerService playerService;
    private final @NotNull EvolutionManager evolutionManager;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;

    public EvolvePrestigeCommand(
        @NotNull PlayerService playerService,
        @NotNull EvolutionManager evolutionManager,
        @NotNull ConfigModel<PrestigeData> prestigeConfig
    ) {
        this.playerService = playerService;
        this.evolutionManager = evolutionManager;
        this.prestigeConfig = prestigeConfig;
    }

    @Command(name = "rankup evoluirprestigio")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.prestigioevoluir")) {
            respond(sender, "Evoluir-prestigio-permissoes");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Evoluir-prestigio-uso");
            return;
        }

        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            respond(sender, "Jogador-offline", message -> message.replace(
                to("@jogador", args[0])
            ));
            return;
        }

        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final Prestige configPrestige = prestigeConfig.data().prestiges().getById(playerModel.prestige());

            if (configPrestige.next() == null) {
                respond(sender, "Evoluir-prestigio-ultimo", message -> message.replace(
                    to("@jogador", player.getName())
                ));
                return;
            }

            final Prestige nextPrestige = prestigeConfig.data().prestiges().getById(configPrestige.next());

            evolutionManager.setPrestige(player, nextPrestige.id());
            respond(sender, "Prestigio-evoluido-comando", message -> message.replace(
                to("@jogador", player.getName()),
                to("@prestigio", nextPrestige.display())
            ));
        });
    }
}
