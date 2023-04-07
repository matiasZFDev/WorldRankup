package com.worldplugins.rankup.database.service;

import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.cache.Cache;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@ExtensionMethod({
    UUIDExtensions.class
})

@RequiredArgsConstructor
public class AutoSavingPlayerService implements PlayerService {
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull PlayerDAO playerDao;
    private final @NonNull Cache<UUID, RankupPlayer> loadedPlayers;

    @Override
    public @NonNull CompletableFuture<Boolean> isRegistered(@NonNull UUID playerId) {
        return playerDao.get(playerId).thenApply(Optional::isPresent);
    }

    @Override
    public void register(@NonNull RankupPlayer player) {
        loadedPlayers.set(player.getId(), player);
        playerDao.save(player);
    }

    @Override
    public boolean isLoaded(@NonNull UUID playerId) {
        return loadedPlayers.containsKey(playerId);
    }

    @Override
    public void load(@NonNull UUID playerId) {
        playerDao.get(playerId).thenAccept(player -> scheduler.newTask(() -> {
            if (!player.isPresent())
                return;

            loadedPlayers.set(playerId, player.get());
        }).run());
    }

    @Override
    public @NonNull RankupPlayer getById(@NonNull UUID playerId) {
        return loadedPlayers.get(playerId);
    }
}
