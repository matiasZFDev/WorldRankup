package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.extension.ViewExtensions;
import com.worldplugins.rankup.view.RankupView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ViewExtensions.class
})

@RequiredArgsConstructor
public class Rankup implements CommandModule {
    private final @NonNull PlayerService playerService;

    @Command(
        name = "rankup",
        target = CommandTarget.PLAYER,
        usage = "&cArgumentos invalidos. Digite /rankup."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;
        playerService.consumePlayer(player.getUniqueId(), playerModel -> player.openView(RankupView.class));
    }
}
