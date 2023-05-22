package com.worldplugins.rankup.manager;

import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.database.service.PlayerService;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EvolutionManager {
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;
    private final @NotNull PermissionManager permissionManager;

    public EvolutionManager(
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<PrestigeData> prestigeConfig,
        @NotNull PermissionManager permissionManager
    ) {
        this.playerService = playerService;
        this.ranksConfig = ranksConfig;
        this.prestigeConfig = prestigeConfig;
        this.permissionManager = permissionManager;
    }

    public void setRank(@NotNull Player player, short rank) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final RanksData.Rank playerRank = ranksConfig.data().getById(playerModel.rank());
            final RanksData.Rank newRank = ranksConfig.data().getById(rank);

            playerModel.setRank(rank);
            permissionManager.removeGroup(player, playerRank.group());
            permissionManager.addGroup(player, newRank.group());
        });
    }

    public void setPrestige(@NotNull Player player, short prestige) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final Prestige playerPrestige = prestigeConfig.data().prestiges().getById(playerModel.prestige());
            final Prestige newPrestige = prestigeConfig.data().prestiges().getById(prestige);

            playerModel.setPrestige(prestige);
            permissionManager.removeGroup(player, playerPrestige.group());
            permissionManager.addGroup(player, newPrestige.group());
        });
    }
}
