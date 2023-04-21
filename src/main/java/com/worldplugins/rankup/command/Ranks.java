package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.rankup.extension.ViewExtensions;
import com.worldplugins.rankup.view.RanksView;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ViewExtensions.class
})

public class Ranks implements CommandModule {
    @Command(
        name = "ranks",
        target = CommandTarget.PLAYER,
        usage = "&cArgumentos invalidos. Digite /ranks."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;
        player.openView(RanksView.class);
    }
}
