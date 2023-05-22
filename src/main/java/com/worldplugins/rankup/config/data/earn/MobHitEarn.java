package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;

public class MobHitEarn extends ShardEarn {
    private final @NotNull EnumSet<EntityType> mobs;

    public MobHitEarn(
        @NotNull Collection<String> ranks,
        @NotNull Collection<String> worlds,
        @NotNull Collection<EntityType> mobs,
        @NotNull Collection<ChanceShard> shards,
        @NotNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.MOB_HIT);
        this.mobs = mobs.isEmpty() ? EnumSet.noneOf(EntityType.class) : EnumSet.copyOf(mobs);
    }

    public @NotNull EnumSet<EntityType> mobs() {
        return mobs;
    }
}
