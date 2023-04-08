package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.ChanceShard;
import com.worldplugins.rankup.config.data.ShardCompensation;
import com.worldplugins.rankup.config.data.ShardSendType;
import lombok.NonNull;

import java.util.Collection;

public class FishEarn extends ShardEarn {
    public FishEarn(
        @NonNull Collection<String> ranks,
        @NonNull Collection<String> worlds,
        @NonNull Collection<ChanceShard> shards,
        @NonNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.FISHING);
    }
}
