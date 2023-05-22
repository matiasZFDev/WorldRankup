package com.worldplugins.rankup.factory;

import com.worldplugins.rankup.database.model.RankupPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface RankupPlayerFactory {
    @NotNull RankupPlayer create(@NotNull UUID playerId);
}
