package com.worldplugins.rankup.command;

import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.view.BagView;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.view.Views;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.rankup.Response.respond;

public class BagCommand implements CommandModule {
    private final @NotNull PlayerService playerService;

    public BagCommand(@NotNull PlayerService playerService) {
        this.playerService = playerService;
    }


    @Command(name = "mochila")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        final Player player = (Player) sender;
        playerService.consumePlayer(player.getUniqueId(), playerModel -> Views.get().open(player, BagView.class));
    }
}
