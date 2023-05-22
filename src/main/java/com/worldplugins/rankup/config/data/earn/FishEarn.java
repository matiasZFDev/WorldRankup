package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FishEarn extends ShardEarn {
    public FishEarn(
        @NotNull Collection<String> ranks,
        @NotNull Collection<String> worlds,
        @NotNull Collection<ChanceShard> shards,
        @NotNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.FISHING);
    }
}
