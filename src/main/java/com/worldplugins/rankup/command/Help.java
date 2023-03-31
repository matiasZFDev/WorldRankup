package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.rankup.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;

@ExtensionMethod({
    ResponseExtensions.class
})

public class Help implements CommandModule {
    @Command(
        name = "rankup ajuda",
        usage = "&cArgumentos invalidos. Digite /rankup ajuda."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        if (sender.hasPermission("worldrankup.ajudastaff"))
            sender.respond("Ajuda-staff");
        else
            sender.respond("Ajuda");
    }
}
