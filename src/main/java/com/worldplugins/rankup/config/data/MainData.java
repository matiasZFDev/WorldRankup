package com.worldplugins.rankup.config.data;

import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MainData {
    public static class ShardSellOptions {
        @RequiredArgsConstructor
        @Getter
        public static class SellBonus {
            private final @NonNull String group;
            private final byte priority;
            private final double bonus;
            private final String tag;
        }

        @Getter
        @Accessors(fluent = true)
        private final boolean useTag;

        @Getter
        private final String noGroupTag;

        private final @NonNull Collection<SellBonus> sellBonusList;

        public ShardSellOptions(
            boolean useTag,
            String noGroupTag,
            @NonNull Collection<SellBonus> sellBonusList
        ) {
            this.useTag = useTag;
            this.noGroupTag = noGroupTag;
            this.sellBonusList = sellBonusList.stream()
                .sorted(Comparator.comparingInt(SellBonus::getPriority).reversed())
                .collect(Collectors.toList());
        }

        /**
         * @return a positive value if present, -1 if not
         * */
        public SellBonus getBonus(@NonNull Player player) {
            return sellBonusList.stream()
                .filter(bonus -> player.hasPermission("group." + bonus.getGroup()))
                .findFirst()
                .orElse(null);
        }
    }

    @Getter
    private final @NonNull ItemDisplay shardDisplay;

    @Getter
    private final @NonNull ItemDisplay limitDisplay;

    private final @NonNull EnumSet<ShardCompensation> shardCompensations;

    private final @NonNull EnumSet<ShardCompensation> limitCompensations;

    @Getter
    private final boolean shardWithdrawEnabled;

    @Getter
    private final ShardSellOptions shardSellOptions;

    public boolean hasShardCompensation(@NonNull ShardCompensation compensation) {
        return shardCompensations.contains(compensation);
    }

    public boolean hasLimitCompensation(@NonNull ShardCompensation compensation) {
        return limitCompensations.contains(compensation);
    }
}
