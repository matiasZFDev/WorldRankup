package com.worldplugins.rankup.database.cache;

import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
import me.post.lib.database.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerCacheUnloadImpl implements CacheUnloadTimer<UUID> {
    private final @NotNull Cache<UUID, RankupPlayer> cache;
    private final @NotNull Set<UUID> unloadCountdown = new HashSet<>();
    private final @NotNull PlayerDAO playerDao;

    public PlayerCacheUnloadImpl(@NotNull Cache<UUID, RankupPlayer> cache, @NotNull PlayerDAO playerDao) {
        this.cache = cache;
        this.playerDao = playerDao;
    }

    @Override
    public void prepareUnload(@NotNull UUID playerId) {
        unloadCountdown.add(playerId);
    }

    public void cancel(@NotNull UUID playerId) {
        unloadCountdown.remove(playerId);
    }

    @Override
    public void unloadAll() {
        final Collection<RankupPlayer> updatablePlayers = unloadCountdown.stream()
                .map(cache::get)
                .filter(RankupPlayer::checkUpdateAndReset)
                .collect(Collectors.toList());

        unloadCountdown.forEach(cache::remove);
        unloadCountdown.clear();
        playerDao.updateAll(updatablePlayers);
    }
}
