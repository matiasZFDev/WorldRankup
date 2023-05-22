package com.worldplugins.rankup.database;

import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import com.worldplugins.rankup.database.cache.PlayerCacheUnloadImpl;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerServiceImpl;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.database.service.ShardBulkUpdater;
import com.worldplugins.rankup.database.service.ShardUpdateQueue;
import com.worldplugins.rankup.init.DatabaseInitializer;
import me.post.lib.config.wrapper.ConfigManager;
import me.post.lib.database.cache.Cache;
import me.post.lib.database.cache.SimpleCache;
import me.post.lib.util.Scheduler;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class DatabaseAccessor {
    private final @NotNull ShardBulkUpdater shardUpdater;
    private final @NotNull PlayerService playerService;
    private final @NotNull CacheUnloadTimer<UUID> cacheUnloader;

    public DatabaseAccessor(
        @NotNull ConfigManager configManager,
        @NotNull Plugin plugin,
        @NotNull Scheduler scheduler
    ) {
        final Cache<UUID, RankupPlayer> cache = new SimpleCache<>(new HashMap<>());
        final PlayerDAO playerDao = new DatabaseInitializer(configManager, plugin).init();
        this.shardUpdater = new ShardUpdateQueue(cache, playerDao);
        this.playerService = new PlayerServiceImpl(scheduler, playerDao, cache);
        this.cacheUnloader = new PlayerCacheUnloadImpl(cache, playerDao);
    }

    public ShardBulkUpdater shardUpdater() {
        return shardUpdater;
    }

    public PlayerService playerService() {
        return playerService;
    }

    public CacheUnloadTimer<UUID> cacheUnloader() {
        return cacheUnloader;
    }
}
