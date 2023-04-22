package com.worldplugins.rankup.database.service;

import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ExtensionMethod({
    UUIDExtensions.class
})

@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull PlayerDAO playerDao;
    private final @NonNull Cache<UUID, RankupPlayer> loadedPlayers;
    private final @NonNull Map<UUID, Queue<Consumer<RankupPlayer>>> playerConsumingQueue = new HashMap<>();

    @Override
    public @NonNull CompletableFuture<Boolean> isRegistered(@NonNull UUID playerId) {
        return playerDao.get(playerId).thenApply(Objects::nonNull);
    }

    @Override
    public void register(@NonNull RankupPlayer player) {
        loadedPlayers.set(player.getId(), player);
        playerDao.save(player);
    }

    @Override
    public void load(@NonNull UUID playerId) {
        playerDao.get(playerId)
            .thenAccept(player -> scheduler.newTask(() -> {
                if (player == null)
                    return;

                loadedPlayers.set(playerId, player);
                runPlayerPendingTasks(player);
            }).run());
    }

    private void runPlayerPendingTasks(@NonNull RankupPlayer player) {
        final Queue<Consumer<RankupPlayer>> pendingTask = playerConsumingQueue.get(player.getId());

        if (pendingTask != null) {
            pendingTask.forEach(task -> task.accept(player));
            playerConsumingQueue.remove(player.getId());
        }
    }

    @Override
    public RankupPlayer getById(@NonNull UUID playerId) {
        return loadedPlayers.get(playerId);
    }

    @Override
    public void consumePlayer(@NonNull UUID playerId, @NonNull Consumer<RankupPlayer> consumer) {
        final RankupPlayer player = getById(playerId);

        if (player != null) {
            consumer.accept(player);
            return;
        }

        playerConsumingQueue.computeIfAbsent(playerId, (x) -> new ArrayDeque<>()).add(consumer);
    }
}
