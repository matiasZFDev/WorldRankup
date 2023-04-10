package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.factory.RankupPlayerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class LoadOnJoinListener implements Listener {
    private final @NonNull PlayerService playerService;
    private final @NonNull RankupPlayerFactory playerFactory;
    private final @NonNull CacheUnloadTimer<UUID> cacheUnloader;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cacheUnloader.cancel(event.getPlayer().getUniqueId());
        playerService.isRegistered(event.getPlayer().getUniqueId()).thenAccept(registered -> {
            if (registered)
                playerService.load(event.getPlayer().getUniqueId());
            else
                playerService.register(playerFactory.create(event.getPlayer().getUniqueId()));
        });
    }
}
