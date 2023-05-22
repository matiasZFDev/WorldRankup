package com.worldplugins.rankup.database.service;

import com.worldplugins.rankup.database.model.RankupPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface PlayerService {
    /**
     * @return Completed true if the player is registered in the database, false if not
     * */
    @NotNull CompletableFuture<Boolean> isRegistered(@NotNull UUID playerId);

    /**
     * Registers a new player using its id and initial model
     * */
    void register(@NotNull RankupPlayer player);

    /**
     * Loads the player from the cache to the database
     * */
    void load(@NotNull UUID playerId);

    /**
     * @return A nullable player from the cache.
     * */
    RankupPlayer getById(@NotNull UUID playerId);

    /**
     * Consumes a player. It can be instantly or delayed, dependings on its load
     * */
    void consumePlayer(@NotNull UUID playerId, @NotNull Consumer<RankupPlayer> player);
}
