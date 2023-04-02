package com.worldplugins.rankup.database.service;

import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.cache.Cache;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import java.util.*;
import java.util.stream.Collectors;

@ExtensionMethod({
    UUIDExtensions.class
})

@RequiredArgsConstructor
public class ShardUpdateQueue implements ShardBulkUpdater {
    private final @NonNull Cache<UUID, RankupPlayer> cache;
    private final @NonNull PlayerDAO playerDAO;
    private final @NonNull Set<UUID> marks = new HashSet<>();

    @Override
    public void mark(@NonNull UUID playerId) {
        marks.add(playerId);
    }

    @Override
    public void update() {
        if (marks.isEmpty())
            return;

        playerDAO.updateAll(
            cache.getValues().stream()
                .filter(player -> marks.contains(player.getId()))
                .collect(Collectors.toList())
        );
    }
}
