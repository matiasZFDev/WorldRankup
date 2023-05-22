package com.worldplugins.rankup.database.dao;

import com.worldplugins.rankup.database.model.RankupPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDAO {
    @NotNull CompletableFuture<RankupPlayer> get(@NotNull UUID playerId);
    void save(@NotNull RankupPlayer player);
    void updateAll(@NotNull Collection<RankupPlayer> players);
}
