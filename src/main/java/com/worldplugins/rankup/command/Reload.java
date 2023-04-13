package com.worldplugins.rankup.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.common.Updatable;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.rankup.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

@ExtensionMethod({
    ResponseExtensions.class
})

public class Reload implements CommandModule {
    private final @NonNull ConfigManager configManager;
    private final @NonNull Updatable[] dependants;

    public Reload(@NonNull ConfigManager configManager, @NonNull Updatable... dependants) {
        this.configManager = configManager;
        this.dependants = dependants;
    }

    @Command(
        name = "rankup reload",
        usage = "&cArgumentos invalidos. Digite /rankup reload.",
        permission = "worldrankup.reload"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        configManager.reloadAll();
        Arrays.asList(dependants).forEach(Updatable::update);
        sender.respond("Configuracao-recarregada");
    }
}
