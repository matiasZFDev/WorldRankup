package com.worldplugins.rankup.manager;

import com.worldplugins.rankup.config.PrestigeConfig;
import com.worldplugins.rankup.config.RanksConfig;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.database.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class EvolutionManager {
    private final @NonNull PlayerService playerService;
    private final @NonNull RanksConfig ranksConfig;
    private final @NonNull PrestigeConfig prestigeConfig;
    private final @NonNull PermissionManager permissionManager;

    public void setRank(@NonNull Player player, short rank) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final RanksConfig.Config.Rank playerRank = ranksConfig.get().getById(playerModel.getRank());
            final RanksConfig.Config.Rank newRank = ranksConfig.get().getPrevious(rank);

            playerModel.setRank(rank);
            permissionManager.removeGroup(player, playerRank.getGroup());
            permissionManager.addGroup(player, newRank.getGroup());
        });
    }

    public void setPrestige(@NonNull Player player, short prestige) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final Prestige configPrestige = prestigeConfig.get().getPrestiges().getById(prestige);
            final Prestige oldPrestige = prestigeConfig.get().getPrestiges().getPrevious(prestige);

            playerModel.setPrestige(prestige);
            permissionManager.addGroup(player, configPrestige.getGroup());

            if (oldPrestige != null)
                permissionManager.removeGroup(player, oldPrestige.getGroup());
        });
    }
}
