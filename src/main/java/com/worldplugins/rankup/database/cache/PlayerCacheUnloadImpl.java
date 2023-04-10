package com.worldplugins.rankup.database.cache;

import com.worldplugins.rankup.database.model.RankupPlayer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class PlayerCacheUnloadImpl implements CacheUnloadTimer<UUID> {
    private final @NonNull Cache<UUID, RankupPlayer> cache;
    private final @NonNull Set<UUID> unloadCountdown = new HashSet<>();

    @Override
    public void prepareUnload(@NonNull UUID playerId) {
        unloadCountdown.add(playerId);
    }

    public void cancel(@NonNull UUID playerId) {
        unloadCountdown.remove(playerId);
    }

    @Override
    public void unloadAll() {
        unloadCountdown.forEach(cache::remove);
        unloadCountdown.clear();
    }
}
