package com.worldplugins.rankup.config.data;

import com.worldplugins.rankup.config.data.earn.ShardEarn;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EarnData {
    private final @NotNull Map<Class<? extends ShardEarn>, List<ShardEarn>> shardEarns;
    private final @NotNull Map<Class<? extends ShardEarn>, List<ShardEarn>> limitEarns;

    public EarnData(
        @NotNull Collection<ShardEarn> shardEarns,
        @NotNull Collection<ShardEarn> limitEarns
    ) {
        this.shardEarns = shardEarns.stream().collect(Collectors.groupingBy(ShardEarn::getClass));
        this.limitEarns = limitEarns.stream().collect(Collectors.groupingBy(ShardEarn::getClass));
    }

    public List<ShardEarn> getShardEarnsByType(@NotNull Class<? extends ShardEarn> earnType) {
        return shardEarns.get(earnType);
    }

    public List<ShardEarn> getLimitEarnsByType(@NotNull Class<? extends ShardEarn> earnType) {
        return limitEarns.get(earnType);
    }
}
