package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
@Getter
public abstract class ShardEarn {
    private final @NonNull Collection<String> ranks;
    private final @NonNull Collection<String> worlds;
    private final @NonNull Collection<ChanceShard> shards;
    private final @NonNull ShardSendType sendType;
    private final @NonNull ShardCompensation compensation;
}
