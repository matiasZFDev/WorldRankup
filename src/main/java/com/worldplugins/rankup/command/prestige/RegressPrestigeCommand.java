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

public class RegressPrestigeCommand implements CommandModule {
    private final @NotNull PlayerService playerService;
    private final @NotNull EvolutionManager evolutionManager;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;

    public RegressPrestigeCommand(
        @NotNull PlayerService playerService,
        @NotNull EvolutionManager evolutionManager,
        @NotNull ConfigModel<PrestigeData> prestigeConfig
    ) {
        this.playerService = playerService;
        this.evolutionManager = evolutionManager;
        this.prestigeConfig = prestigeConfig;
    }

    @Command(name = "rankup regredirprestigio")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.prestigioregredir")) {
            respond(sender, "Regredir-prestigio-permissoes");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Regredir-prestigio-uso");
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
            final Prestige previousPrestige = prestigeConfig.data().prestiges().getPrevious(configPrestige.id());

            if (previousPrestige == null) {
                respond(sender, "Regredir-prestigio-primeiro", message -> message.replace(
                    to("@jogador", player.getName())
                ));
                return;
            }

            evolutionManager.setPrestige(player, previousPrestige.id());
            respond(sender, "Prestigio-regredido", message -> message.replace(
                to("@jogador", player.getName()),
                to("@prestigio", previousPrestige.display())
            ));
        });
    }
}
