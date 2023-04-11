package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.RankupPlayerFactory;
import com.worldplugins.rankup.manager.EvolutionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class LoadOnJoinListener implements Listener {
    private final @NonNull PlayerService playerService;
    private final @NonNull RankupPlayerFactory playerFactory;
    private final @NonNull CacheUnloadTimer<UUID> cacheUnloader;
    private final @NonNull EvolutionManager evolutionManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        cacheUnloader.cancel(player.getUniqueId());
        playerService.isRegistered(player.getUniqueId()).thenAccept(registered -> {
            if (registered)
                playerService.load(player.getUniqueId());
            else {
                playerService.register(playerFactory.create(player.getUniqueId()));
                final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
                evolutionManager.setRank(player, playerModel.getRank());
                evolutionManager.setPrestige(player, playerModel.getPrestige());
            }
        });
    }
}
