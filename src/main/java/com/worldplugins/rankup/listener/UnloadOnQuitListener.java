package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
public class UnloadOnQuitListener implements Listener {
    private final @NotNull CacheUnloadTimer<UUID> cacheUnloader;

    public UnloadOnQuitListener(@NotNull CacheUnloadTimer<UUID> cacheUnloader) {
        this.cacheUnloader = cacheUnloader;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cacheUnloader.prepareUnload(event.getPlayer().getUniqueId());
    }
}
