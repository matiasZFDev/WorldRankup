package com.worldplugins.rankup.manager;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.database.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class EvolutionManager {
    private final @NonNull PlayerService playerService;
    private final @NonNull ConfigCache<RanksData> ranksConfig;
    private final @NonNull ConfigCache<PrestigeData> prestigeConfig;
    private final @NonNull PermissionManager permissionManager;

    public void setRank(@NonNull Player player, short rank) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final RanksData.Rank playerRank = ranksConfig.data().getById(playerModel.getRank());
            final RanksData.Rank newRank = ranksConfig.data().getById(rank);

            playerModel.setRank(rank);
            permissionManager.removeGroup(player, playerRank.getGroup());
            permissionManager.addGroup(player, newRank.getGroup());
        });
    }

    public void setPrestige(@NonNull Player player, short prestige) {
        playerService.consumePlayer(player.getUniqueId(), playerModel -> {
            final Prestige playerPrestige = prestigeConfig.data().getPrestiges().getById(playerModel.getPrestige());
            final Prestige newPrestige = prestigeConfig.data().getPrestiges().getById(prestige);

            playerModel.setPrestige(prestige);
            permissionManager.removeGroup(player, playerPrestige.getGroup());
            permissionManager.addGroup(player, newPrestige.getGroup());
        });
    }
}
