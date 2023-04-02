package com.worldplugins.rankup.database.dao;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.model.Shard;
import com.worldplugins.rankup.database.model.RankupPlayerImpl;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ExtensionMethod({
    UUIDExtensions.class
})

public class SQLPlayerDAO implements PlayerDAO {
    private final @NonNull SQLExecutor sqlExecutor;

    private static final @NonNull String RANK_TABLE = "worldrankup_rank";
    private static final @NonNull String SHARDS_TABLE = "worldrankup_fragmentos";
    private static final @NonNull Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public SQLPlayerDAO(@NonNull SQLExecutor sqlExecutor) {
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
    public @NonNull CompletableFuture<Optional<RankupPlayer>> get(@NonNull UUID playerId) {
        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT * FROM " + RANK_TABLE + " WHERE player_id=?",
                statement -> statement.set(1, playerId.getBytes()),
                result -> result.next()
                    ? Optional.of(new PlayerData(result.get("rank"), result.get("prestige")))
                    : Optional.<PlayerData>empty()
            ), EXECUTOR)
            .thenApplyAsync(playerData -> {
                if (!playerData.isPresent())
                    return Optional.empty();

                return sqlExecutor.executeQuery(
                    "SELECT shard_id, amount, capacity FROM " + SHARDS_TABLE + " WHERE player_id=?",
                    statement -> statement.set(1, playerId.getBytes()),
                    result -> {

                        final Collection<Shard> shards = new ArrayList<>(10);

                        while (result.next()) {
                            shards.add(new Shard(
                                result.get("shard_id"),
                                result.get("amount"),
                                result.get("capacity")
                            ));
                        }

                        return Optional.of(new RankupPlayerImpl(
                            playerId, playerData.get().getRank(), playerData.get().getPrestige(), shards
                        ));
                    }
                );
            }, EXECUTOR);
    }

    @Override
    public void save(@NonNull RankupPlayer player) {
        CompletableFuture.runAsync(() -> {
            sqlExecutor.update(
                "INSERT INTO " + RANK_TABLE + "(player_id, rank) VALUES (?, ?)",
                statement -> {
                    statement.set(1, player.getId().getBytes());
                    statement.set(2, player.getRank());
                }
            );

            final Collection<Shard> shards = player.getAllShards();

            if (shards.isEmpty())
                return;

            sqlExecutor.update(
                "INSERT INTO " + SHARDS_TABLE + "(player_id, shard_id, amount, capacity) VALUES (?, ?, ?, ?)",
                statement ->
                    shards.forEach(shard -> {
                        statement.set(1, player.getId().getBytes());
                        statement.set(2, shard.getId());
                        statement.set(3, shard.getAmount());
                        statement.set(4, shard.getLimit());
                        statement.addBatch();
                    })
            );
        }, EXECUTOR);
    }

    @Override
    public void updateAll(@NonNull Collection<RankupPlayer> players) {
        CompletableFuture.runAsync(() -> {
            sqlExecutor.update(
                "UPDATE TABLE " + RANK_TABLE + " SET rank=?, prestige=? WHERE player_id=?",
                statement -> {
                    players.forEach(player -> {
                        statement.set(1, player.getRank());
                        statement.set(2, player.getPrestige());
                        statement.set(3, player.getId().getBytes());
                    });
                }
            );
            sqlExecutor.update(
                "UPDATE TABLE " + RANK_TABLE + " SET amount=?, capacity=? WHERE player_id=? AND shard_id=?",
                statement -> {
                    players.forEach(player -> {
                        player.getAllShards().forEach(shard -> {
                            statement.set(1, shard.getAmount());
                            statement.set(2, shard.getLimit());
                            statement.set(3, player.getId().getBytes());
                            statement.set(4, shard.getId());
                        });
                    });
                }
            );
        });
    }
}
