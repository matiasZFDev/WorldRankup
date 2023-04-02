package com.worldplugins.rankup.database.service;

import lombok.NonNull;

import java.util.UUID;

public interface ShardBulkUpdater {
    void mark(@NonNull UUID playerId);
    void update();
}
