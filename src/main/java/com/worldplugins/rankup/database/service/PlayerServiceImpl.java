package com.worldplugins.rankup.database.service;

import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import me.post.lib.database.cache.Cache;
import me.post.lib.util.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
public class PlayerServiceImpl implements PlayerService {
    private final @NotNull Scheduler scheduler;
    private final @NotNull PlayerDAO playerDao;
    private final @NotNull Cache<UUID, RankupPlayer> loadedPlayers;
    private final @NotNull Map<UUID, Queue<Consumer<RankupPlayer>>> playerConsumingQueue = new HashMap<>();

    public PlayerServiceImpl(@NotNull Scheduler scheduler, @NotNull PlayerDAO playerDao, @NotNull Cache<UUID, RankupPlayer> loadedPlayers) {
        this.scheduler = scheduler;
        this.playerDao = playerDao;
        this.loadedPlayers = loadedPlayers;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isRegistered(@NotNull UUID playerId) {
        return playerDao.get(playerId).thenApply(Objects::nonNull);
    }

    @Override
    public void register(@NotNull RankupPlayer player) {
        loadedPlayers.set(player.id(), player);
        playerDao.save(player);
    }

    @Override
    public void load(@NotNull UUID playerId) {
        playerDao.get(playerId).thenAccept(player -> scheduler.runTask(0, false, () -> {
            if (player == null) {
                return;
            }

            loadedPlayers.set(playerId, player);
            runPlayerPendingTasks(player);
        }));
    }

    private void runPlayerPendingTasks(@NotNull RankupPlayer player) {
        final Queue<Consumer<RankupPlayer>> pendingTask = playerConsumingQueue.get(player.id());

        if (pendingTask != null) {
            pendingTask.forEach(task -> task.accept(player));
            playerConsumingQueue.remove(player.id());
        }
    }

    @Override
    public RankupPlayer getById(@NotNull UUID playerId) {
        return loadedPlayers.get(playerId);
    }

    @Override
    public void consumePlayer(@NotNull UUID playerId, @NotNull Consumer<RankupPlayer> consumer) {
        final RankupPlayer player = getById(playerId);

        if (player != null) {
            consumer.accept(player);
            return;
        }

        playerConsumingQueue.computeIfAbsent(playerId, (x) -> new ArrayDeque<>()).add(consumer);
    }
}
