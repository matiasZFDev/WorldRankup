package com.worldplugins.rankup.database.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface RankupPlayer {
    @NotNull UUID id();

    short rank();

    void setRank(short rank);

    short prestige();

    void setPrestige(short prestige);

    @NotNull Collection<Shard> getAllShards();

    int getShards(byte shardId);

    /**
     * @return the real amount that was set computing its limit
     * */
    int setShards(byte shardId, int amount);

    int getShardLimit(byte shardId);

    void setShardLimit(byte shardId, int amount);

    boolean checkUpdateAndReset();
}