package com.worldplugins.rankup.database.service;

import com.worldplugins.rankup.database.model.RankupPlayer;
import lombok.NonNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerService {
    /**
     * @return completed true if the player is registered in the database, false if not
     * */
    @NonNull CompletableFuture<Boolean> isRegistered(@NonNull UUID playerId);

    /**
     * Registers a new player using its id and initial model
     * */
    void register(@NonNull RankupPlayer player);

    /**
     * @return true if the player is loaded in cache, false if not
     * */
    boolean isLoaded(@NonNull UUID playerId);

    /**
     * Loads the player from the cache to the database
     * */
    void load(@NonNull UUID playerId);

    /**
     * @return a non-null player from the cache. A load check must be executed before
     * */
    @NonNull RankupPlayer getById(@NonNull UUID playerId);

    void update(@NonNull RankupPlayer player);
}
