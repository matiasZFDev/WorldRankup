package com.worldplugins.rankup.factory;

import com.worldplugins.rankup.database.model.RankupPlayer;
import lombok.NonNull;

import java.util.UUID;

public interface RankupPlayerFactory {
    @NonNull RankupPlayer create(@NonNull UUID playerId);
}
