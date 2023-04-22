package com.worldplugins.rankup.database;

import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.lib.util.cache.SimpleCache;
import com.worldplugins.rankup.database.cache.CacheUnloadTimer;
import com.worldplugins.rankup.database.cache.PlayerCacheUnloadImpl;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerServiceImpl;
import com.worldplugins.rankup.database.service.PlayerService;
import com.worldplugins.rankup.database.service.ShardBulkUpdater;
import com.worldplugins.rankup.database.service.ShardUpdateQueue;
import com.worldplugins.rankup.init.DatabaseInitializer;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class DatabaseAccessor {
    @Getter
    private final @NonNull ShardBulkUpdater shardUpdater;
    @Getter
    private final @NonNull PlayerService playerService;
    @Getter
    private final @NonNull CacheUnloadTimer<UUID> cacheUnloader;

    public DatabaseAccessor(
        @NonNull ConfigManager configManager,
        @NonNull Plugin plugin,
        @NonNull SchedulerBuilder scheduler
    ) {
        final Cache<UUID, RankupPlayer> cache = new SimpleCache<>(new HashMap<>());
        final PlayerDAO playerDao = new DatabaseInitializer(configManager, plugin).init();
        this.shardUpdater = new ShardUpdateQueue(cache, playerDao);
        this.playerService = new PlayerServiceImpl(scheduler, playerDao, cache);
        this.cacheUnloader = new PlayerCacheUnloadImpl(cache, playerDao);
    }
}
