package com.worldplugins.rankup.database.service;

import com.worldplugins.rankup.database.model.RankupPlayer;
import lombok.NonNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
     * Loads the player from the cache to the database
     * */
    void load(@NonNull UUID playerId);

    /**
     * @return A nullable player from the cache.
     * */
    RankupPlayer getById(@NonNull UUID playerId);

    /**
     * Consumes a player. It can be instantly or delayed, dependings on its load
     * */
    void consumePlayer(@NonNull UUID playerId, @NonNull Consumer<RankupPlayer> player);
}
