package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;

public class MobKillEarn extends ShardEarn {
    private final @NotNull EnumSet<EntityType> mobs;

    public MobKillEarn(
        @NotNull Collection<String> ranks,
        @NotNull Collection<String> worlds,
        @NotNull Collection<EntityType> mobs,
        @NotNull Collection<ChanceShard> shards,
        @NotNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.MOB_KILL);
        this.mobs = mobs.isEmpty() ? EnumSet.noneOf(EntityType.class) : EnumSet.copyOf(mobs);
    }

    public @NotNull EnumSet<EntityType> mobs() {
        return mobs;
    }
}
