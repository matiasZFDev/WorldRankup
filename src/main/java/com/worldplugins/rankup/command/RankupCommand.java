package com.worldplugins.rankup.command;

import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.view.RankupView;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.view.Views;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.rankup.Response.respond;

public class RankupCommand implements CommandModule {
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<RanksData> ranksConfig;

    public RankupCommand(@NotNull PlayerService playerService, @NotNull ConfigModel<RanksData> ranksConfig) {
        this.playerService = playerService;
        this.ranksConfig = ranksConfig;
    }

    @Command(name = "rankup")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        final Player player = (Player) sender;
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            if (ranksConfig.data().getById(playerModel.rank()).evolution() == null) {
                respond(player, "Rank-evoluir-ultimo");
                return;
            }

            Views.get().open(player, RankupView.class);
        });
    }
}
