package com.worldplugins.rankup.config.data;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class MainData {


    public static class ShardSellOptions {
        public static class SellBonus {
            private final @NotNull String group;
            private final byte priority;
            private final double bonus;
            private final @Nullable String tag;

            public SellBonus(@NotNull String group, byte priority, double bonus, @Nullable String tag) {
                this.group = group;
                this.priority = priority;
                this.bonus = bonus;
                this.tag = tag;
            }

            public @NotNull String group() {
                return group;
            }

            public byte priority() {
                return priority;
            }

            public double bonus() {
                return bonus;
            }

            public @Nullable String tag() {
                return tag;
            }
        }

        private final boolean useTag;

        private final @NotNull String noGroupTag;

        private final @NotNull Collection<SellBonus> sellBonusList;

        public ShardSellOptions(
            boolean useTag,
            @NotNull String noGroupTag,
            @NotNull Collection<SellBonus> sellBonusList
        ) {
            this.useTag = useTag;
            this.noGroupTag = noGroupTag;
            this.sellBonusList = sellBonusList.stream()
                .sorted(Comparator.comparingInt(SellBonus::priority).reversed())
                .collect(Collectors.toList());
        }

        public boolean useTag() {
            return useTag;
        }

        public @NotNull String noGroupTag() {
            return noGroupTag;
        }

        public @Nullable SellBonus getBonus(@NotNull Player player) {
            return sellBonusList.stream()
                .filter(bonus -> player.hasPermission("group." + bonus.group()))
                .findFirst()
                .orElse(null);
        }
    }

    private final @NotNull ItemDisplay shardDisplay;

    private final @NotNull ItemDisplay limitDisplay;

    private final @NotNull EnumSet<ShardCompensation> shardCompensations;

    private final @NotNull EnumSet<ShardCompensation> limitCompensations;

    private final boolean shardWithdrawEnabled;

    private final ShardSellOptions shardSellOptions;

    public MainData(
        @NotNull ItemDisplay shardDisplay,
        @NotNull ItemDisplay limitDisplay,
        @NotNull EnumSet<ShardCompensation> shardCompensations,
        @NotNull EnumSet<ShardCompensation> limitCompensations,
        boolean shardWithdrawEnabled,
        @Nullable ShardSellOptions shardSellOptions
    ) {
        this.shardDisplay = shardDisplay;
        this.limitDisplay = limitDisplay;
        this.shardCompensations = shardCompensations;
        this.limitCompensations = limitCompensations;
        this.shardWithdrawEnabled = shardWithdrawEnabled;
        this.shardSellOptions = shardSellOptions;
    }

    public @NotNull ItemDisplay shardDisplay() {
        return shardDisplay;
    }

    public @NotNull ItemDisplay limitDisplay() {
        return limitDisplay;
    }

    public boolean isShardWithdrawEnabled() {
        return shardWithdrawEnabled;
    }

    public @Nullable ShardSellOptions shardSellOptions() {
        return shardSellOptions;
    }

    public boolean hasShardCompensation(@NotNull ShardCompensation compensation) {
        return shardCompensations.contains(compensation);
    }

    public boolean hasLimitCompensation(@NotNull ShardCompensation compensation) {
        return limitCompensations.contains(compensation);
    }
}
