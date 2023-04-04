package com.worldplugins.rankup.database.model;

import lombok.*;

import java.util.Collection;
import java.util.UUID;

public interface RankupPlayer {
    @NonNull UUID getId();

    short getRank();

    void setRank(short rank);

    short getPrestige();

    void setPrestige(short prestige);

    @NonNull Collection<Shard> getAllShards();

    int getShards(byte shardId);

    /**
     * @return the real amount that was set computing its limit
     * */
    int setShards(byte shardId, int amount);

    int getShardLimit(byte shardId);

    void setShardLimit(byte shardId, int amount);
}