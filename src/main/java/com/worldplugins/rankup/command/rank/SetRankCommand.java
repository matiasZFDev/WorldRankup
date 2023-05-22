package com.worldplugins.rankup.command.rank;

import com.worldplugins.rankup.config.data.RanksData;
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

public class SetRankCommand implements CommandModule {
    private final @NotNull EvolutionManager evolutionManager;
    private final @NotNull ConfigModel<RanksData> ranksConfig;

    public SetRankCommand(@NotNull EvolutionManager evolutionManager, @NotNull ConfigModel<RanksData> ranksConfig) {
        this.evolutionManager = evolutionManager;
        this.ranksConfig = ranksConfig;
    }

    @Command(name = "rankup setrank")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.setrank")) {
            respond(sender, "Setar-rank-permissoes");
            return;
        }

        if (args.length != 2) {
            respond(sender, "Setar-rank-uso");
            return;
        }

        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            respond(sender, "Jogador-offline", message -> message.replace(
                to("@jogador", args[0])
            ));
            return;
        }

        final RanksData.Rank configRank = ranksConfig.data().getByName(args[1]);

        if (configRank == null) {
            respond(sender, "Setar-rank-invalido", message -> message.replace(
                to("@rank", args[1])
            ));
            return;
        }

        evolutionManager.setRank(player, configRank.id());
        respond(sender, "Rank-setado", message -> message.replace(
            to("@jogador", player.getName()),
            to("@rank", configRank.display())
        ));
    }
}
