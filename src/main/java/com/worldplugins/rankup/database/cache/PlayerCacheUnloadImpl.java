package com.worldplugins.rankup.database.cache;

import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PlayerCacheUnloadImpl implements CacheUnloadTimer<UUID> {
    private final @NonNull Cache<UUID, RankupPlayer> cache;
    private final @NonNull Set<UUID> unloadCountdown = new HashSet<>();
    private final @NonNull PlayerDAO playerDao;

    @Override
    public void prepareUnload(@NonNull UUID playerId) {
        unloadCountdown.add(playerId);
    }

    public void cancel(@NonNull UUID playerId) {
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
