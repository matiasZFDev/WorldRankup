package com.worldplugins.rankup.database.dao;

import com.worldplugins.rankup.database.model.RankupPlayer;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDAO {
    @NonNull CompletableFuture<Optional<RankupPlayer>> get(@NonNull UUID playerId);
    void save(@NonNull RankupPlayer player);
    void updateAll(@NonNull Collection<RankupPlayer> players);
}
