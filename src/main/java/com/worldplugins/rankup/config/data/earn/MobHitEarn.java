package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.EnumSet;

public class MobHitEarn extends ShardEarn {
    @Getter
    private final @NonNull EnumSet<EntityType> mobs;

    public MobHitEarn(
        @NonNull Collection<String> ranks,
        @NonNull Collection<String> worlds,
        @NonNull Collection<EntityType> mobs,
        @NonNull Collection<ChanceShard> shards,
        @NonNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.MOB_HIT);
        this.mobs = mobs.isEmpty() ? EnumSet.noneOf(EntityType.class) : EnumSet.copyOf(mobs);
    }
}
