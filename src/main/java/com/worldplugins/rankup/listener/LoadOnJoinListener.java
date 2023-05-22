package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.RankupPlayerFactory;
import com.worldplugins.rankup.manager.EvolutionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LoadOnJoinListener implements Listener {
    private final @NotNull PlayerService playerService;
    private final @NotNull RankupPlayerFactory playerFactory;
    private final @NotNull CacheUnloadTimer<UUID> cacheUnloader;
    private final @NotNull EvolutionManager evolutionManager;

    public LoadOnJoinListener(
        @NotNull PlayerService playerService,
        @NotNull RankupPlayerFactory playerFactory,
        @NotNull CacheUnloadTimer<UUID> cacheUnloader,
        @NotNull EvolutionManager evolutionManager
    ) {
        this.playerService = playerService;
        this.playerFactory = playerFactory;
        this.cacheUnloader = cacheUnloader;
        this.evolutionManager = evolutionManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        cacheUnloader.cancel(player.getUniqueId());

        playerService.isRegistered(player.getUniqueId()).thenAccept(registered -> {
            if (registered) {
                playerService.load(player.getUniqueId());
                return;
            }

            playerService.register(playerFactory.create(player.getUniqueId()));
            final RankupPlayer playerModel = playerService.getById(player.getUniqueId());
            evolutionManager.setRank(player, playerModel.rank());
            evolutionManager.setPrestige(player, playerModel.prestige());
        });
    }
}
