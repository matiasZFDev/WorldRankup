package com.worldplugins.rankup.command;

import com.worldplugins.rankup.view.RanksView;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.view.Views;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RanksCommand implements CommandModule {
    @Command(name = "ranks")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        final Player player = (Player) sender;
        Views.get().open(player, RanksView.class, new RanksView.Context((byte) 0));
    }
}
