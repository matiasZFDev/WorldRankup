package com.worldplugins.rankup.database.dao;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.model.Shard;
import com.worldplugins.rankup.database.model.RankupPlayerImpl;
import me.post.lib.util.UUIDs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SQLPlayerDAO implements PlayerDAO {
    private final @NotNull SQLExecutor sqlExecutor;

    private static final @NotNull String RANK_TABLE = "worldrankup_rank";
    private static final @NotNull String SHARDS_TABLE = "worldrankup_fragmentos";
    private static final @NotNull Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public SQLPlayerDAO(@NotNull SQLExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
        createTables();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + RANK_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "rank SMALLINT NOT NULL, " +
                "prestige SMALLINT NOT NULL, " +
                "PRIMARY KEY(player_id)" +
            ")"
        );
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + SHARDS_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "shard_id TINYINT NOT NULL, " +
                "amount INT NOT NULL, " +
                "capacity INT NOT NULL" +
            ")"
        );
    }

    @Override
    public @NotNull CompletableFuture<RankupPlayer> get(@NotNull UUID playerId) {
        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT rank, prestige FROM " + RANK_TABLE + " WHERE player_id=?",
                statement -> statement.set(1, UUIDs.getBytes(playerId)),
                result -> result.next()
                    ? new PlayerData(
                        result.get("rank", Short.class),
                        result.get("prestige", Short.class)
                    )
                    : null
            ), EXECUTOR)
            .thenApplyAsync(playerData -> {
                if (playerData == null)
                    return null;

                return sqlExecutor.executeQuery(
                    "SELECT shard_id, amount, capacity FROM " + SHARDS_TABLE + " WHERE player_id=?",
                    statement -> statement.set(1, UUIDs.getBytes(playerId)),
                    result -> {

                        final Collection<Shard> shards = new ArrayList<>(10);

                        while (result.next()) {
                            shards.add(new Shard(
                                result.get("shard_id", Byte.class),
                                result.get("amount"),
                                result.get("capacity")
                            ));
                        }

                        return new RankupPlayerImpl(
                            playerId, playerData.rank(), playerData.prestige(), shards
                        );
                    }
                );
            }, EXECUTOR);
    }

    @Override
    public void save(@NotNull RankupPlayer player) {
        CompletableFuture.runAsync(() -> {
            sqlExecutor.update(
                "INSERT INTO " + RANK_TABLE + "(player_id, rank, prestige) VALUES (?, ?, ?)",
                statement -> {
                    statement.set(1, UUIDs.getBytes(player.id()));
                    statement.set(2, player.rank());
                    statement.set(3, player.prestige());
                }
            );

            final Collection<Shard> shards = player.getAllShards();

            if (shards.isEmpty())
                return;

            sqlExecutor.executeBatch(
                "INSERT INTO " + SHARDS_TABLE + "(player_id, shard_id, amount, capacity) VALUES (?, ?, ?, ?)",
                statement ->
                    shards.forEach(shard -> {
                        statement.set(1, UUIDs.getBytes(player.id()));
                        statement.set(2, shard.id());
                        statement.set(3, shard.amount());
                        statement.set(4, shard.limit());
                        statement.addBatch();
                    })
            );
        }, EXECUTOR);
    }

    @Override
    public void updateAll(@NotNull Collection<RankupPlayer> players) {
        CompletableFuture.runAsync(() -> {
            sqlExecutor.update(
                "UPDATE " + RANK_TABLE + " SET rank=?, prestige=? WHERE player_id=?",
                statement -> players.forEach(player -> {
                    statement.set(1, player.rank());
                    statement.set(2, player.prestige());
                    statement.set(3, UUIDs.getBytes(player.id()));
                })
            );
            sqlExecutor.update(
                "UPDATE " + SHARDS_TABLE + " SET amount=?, capacity=? WHERE player_id=? AND shard_id=?",
                statement -> players.forEach(player -> player.getAllShards().forEach(shard -> {
                    statement.set(1, shard.amount());
                    statement.set(2, shard.limit());
                    statement.set(3, UUIDs.getBytes(player.id()));
                    statement.set(4, shard.id());
                }))
            );
        }, EXECUTOR);
    }
}
