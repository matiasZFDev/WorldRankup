package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class ShardEarn {
    private final @NotNull Collection<String> ranks;
    private final @NotNull Collection<String> worlds;
    private final @NotNull Collection<ChanceShard> shards;
    private final @NotNull ShardSendType sendType;
    private final @NotNull ShardCompensation compensation;

    protected ShardEarn(
        @NotNull Collection<String> ranks,
        @NotNull Collection<String> worlds,
        @NotNull Collection<ChanceShard> shards,
        @NotNull ShardSendType sendType,
        @NotNull ShardCompensation compensation
    ) {
        this.ranks = ranks;
        this.worlds = worlds;
        this.shards = shards;
        this.sendType = sendType;
        this.compensation = compensation;
    }

    public @NotNull Collection<String> ranks() {
        return ranks;
    }

    public @NotNull Collection<String> worlds() {
        return worlds;
    }

    public @NotNull Collection<ChanceShard> shards() {
        return shards;
    }

    public @NotNull ShardSendType sendType() {
        return sendType;
    }

    public @NotNull ShardCompensation compensation() {
        return compensation;
    }
}
