package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class UnloadOnQuitListener implements Listener {
    private final @NonNull CacheUnloadTimer<UUID> cacheUnloader;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cacheUnloader.prepareUnload(event.getPlayer().getUniqueId());
    }
}
