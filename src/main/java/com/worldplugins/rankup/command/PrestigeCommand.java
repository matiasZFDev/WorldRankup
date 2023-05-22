package com.worldplugins.rankup.command;

import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.view.PrestigeView;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.view.Views;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PrestigeCommand implements CommandModule {
    private final @NotNull PlayerService playerService;

    public PrestigeCommand(@NotNull PlayerService playerService) {
        this.playerService = playerService;
    }

    @Command(name = "prestigio")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        final Player player = (Player) sender;
        playerService.consumePlayer(player.getUniqueId(), playerModel -> Views.get().open(player, PrestigeView.class));
    }
}
