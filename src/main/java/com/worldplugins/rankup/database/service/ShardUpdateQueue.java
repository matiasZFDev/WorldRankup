package com.worldplugins.rankup.database.service;

import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
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

    @Override
    public void update() {
        playerDAO.updateAll(
            cache.getValues().stream()
                .filter(RankupPlayer::checkUpdateAndReset)
                .collect(Collectors.toList())
        );
    }
}
