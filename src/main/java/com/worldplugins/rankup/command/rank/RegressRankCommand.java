package com.worldplugins.rankup.command.rank;

import com.worldplugins.rankup.config.data.RanksData;
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

public class RegressRankCommand implements CommandModule {
    private final @NotNull PlayerService playerService;
    private final @NotNull EvolutionManager evolutionManager;
    private final @NotNull ConfigModel<RanksData> ranksConfig;

    public RegressRankCommand(
        @NotNull PlayerService playerService,
        @NotNull EvolutionManager evolutionManager,
        @NotNull ConfigModel<RanksData> ranksConfig
    ) {
        this.playerService = playerService;
        this.evolutionManager = evolutionManager;
        this.ranksConfig = ranksConfig;
    }

    @Command(name = "rankup regredir")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.rankregredir")) {
            respond(sender, "Regredir-rank-permissoes");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Regredir-rank-uso");
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
            final RanksData.Rank configRank = ranksConfig.data().getById(playerModel.rank());
            final RanksData.Rank previousRank = ranksConfig.data().getPrevious(configRank.id());

            if (previousRank == null) {
                respond(sender, "Regredir-rank-primeiro", message -> message.replace(
                    to("@jogador", player.getName())
                ));
                return;
            }

            evolutionManager.setRank(player, previousRank.id());
            respond(sender, "Rank-regredido", message -> message.replace(
                to("@jogador", player.getName()),
                to("@rank", previousRank.display())
            ));
        });
    }
}
