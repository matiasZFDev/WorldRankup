package com.worldplugins.rankup.manager;

import com.worldplugins.rankup.config.RanksConfig;
import com.worldplugins.rankup.database.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class EvolutionManager {
    private final @NonNull PlayerService playerService;
    private final @NonNull RanksConfig ranksConfig;
    private final @NonNull PermissionManager permissionManager;

    public void setRank(@NonNull Player player, short rank) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final RanksConfig.Config.Rank configRank = ranksConfig.get().getById(rank);
            final RanksConfig.Config.Rank previousRank = ranksConfig.get().getPrevious(rank);

            playerModel.setRank(rank);
            permissionManager.addGroup(player, configRank.getGroup());

            if (previousRank != null) {
                permissionManager.removeGroup(player, previousRank.getGroup());
            }
        });
    }
}
