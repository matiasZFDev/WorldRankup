package com.worldplugins.rankup.command.prestige;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberExtensions;
import com.worldplugins.rankup.config.PrestigeConfig;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.manager.EvolutionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberExtensions.class
})

@RequiredArgsConstructor
public class SetPrestige implements CommandModule {
    private final @NonNull EvolutionManager evolutionManager;
    private final @NonNull PrestigeConfig prestigeConfig;

    @Command(
        name = "rankup setprestigio",
        permission = "worldrankup.setprestigio",
        argsChecks = {@ArgsChecker(size = 2)},
        usage = "&cArgumentos invalidos. Digite /rankup setprestigio <jogador> <prestigio>."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.respond("Jogador-offline", message -> message.replace(
                "@jogador".to(args[0])
            ));
            return;
        }

        final Short prestige = args[1].equals("0") ? Short.valueOf("0") : args[1].toShortOrNull();

        if (prestige == null) {
            sender.respond("Prestigio-invalido", message -> message.replace(
                "@prestigio".to(args[1])
            ));
            return;
        }

        System.out.println("AFTER");
        System.out.println(prestige);

        final Prestige configPrestige = prestigeConfig.get().getPrestiges().getById(prestige);

        if (configPrestige == null) {
            sender.respond("Prestigio-invalido", message -> message.replace(
                "@prestigio".to(args[1])
            ));
            return;
        }

        evolutionManager.setPrestige(player, prestige);
        sender.respond("Prestigio-setado", message -> message.replace(
            "@jogador".to(player.getName()),
            "@prestigio".to(configPrestige.getDisplay())
        ));
    }
}
