package com.worldplugins.rankup.database.service;

import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.model.RankupPlayer;
import me.post.lib.database.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ShardUpdateQueue implements ShardBulkUpdater {
    private final @NotNull Cache<UUID, RankupPlayer> cache;
    private final @NotNull PlayerDAO playerDAO;

    public ShardUpdateQueue(@NotNull Cache<UUID, RankupPlayer> cache, @NotNull PlayerDAO playerDAO) {
        this.cache = cache;
        this.playerDAO = playerDAO;
    }

    @Override
    public void update() {
        playerDAO.updateAll(
            cache.getValues().stream()
                .filter(RankupPlayer::checkUpdateAndReset)
                .collect(Collectors.toList())
        );
    }
}
