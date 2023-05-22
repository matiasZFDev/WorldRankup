package com.worldplugins.rankup.command.prestige;

import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.manager.EvolutionManager;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.rankup.Response.respond;
import static me.post.lib.util.Pairs.to;

public class SetPrestigeCommand implements CommandModule {
    private final @NotNull EvolutionManager evolutionManager;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;

    public SetPrestigeCommand(@NotNull EvolutionManager evolutionManager, @NotNull ConfigModel<PrestigeData> prestigeConfig) {
        this.evolutionManager = evolutionManager;
        this.prestigeConfig = prestigeConfig;
    }

    @Command(name = "rankup setarprestigio")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldrankup.setprestigio")) {
            respond(sender, "Setar-prestigio-permissoes");
            return;
        }

        if (args.length != 2) {
            respond(sender, "Setar-prestigio-uso");
            return;
        }

        final Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            respond(sender, "Jogador-offline", message -> message.replace(
                to("@jogador", args[0])
            ));
            return;
        }

        final Short prestige = args[1].equals("0")
            ? Short.valueOf("0")
            : Numbers.toShortOrNull(args[1]);

        if (prestige == null) {
            respond(sender, "Prestigio-invalido", message -> message.replace(
                to("@prestigio", args[1])
            ));
            return;
        }

        final Prestige configPrestige = prestigeConfig.data().prestiges().getById(prestige);

        if (configPrestige == null) {
            respond(sender, "Prestigio-invalido", message -> message.replace(
                to("@prestigio", args[1])
            ));
            return;
        }

        evolutionManager.setPrestige(player, prestige);
        respond(sender, "Prestigio-setado", message -> message.replace(
            to("@jogador", player.getName()),
            to("@prestigio", configPrestige.display())
        ));
    }
}
