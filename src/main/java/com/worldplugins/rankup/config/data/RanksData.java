package com.worldplugins.rankup.config.data;

import com.worldplugins.lib.extension.CollectionExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ExtensionMethod({
    CollectionExtensions.class
})

@RequiredArgsConstructor
public class RanksData {
    @RequiredArgsConstructor
    @Getter
    public static class Rank {
        @RequiredArgsConstructor
        @Getter
        public static class Evolution {
            @RequiredArgsConstructor
            @Getter
            public static class ShardRequirement {
                private final @NonNull String name;
                private final int amount;
            }

            private final double coinsPrice;
            private final @NonNull Collection<ShardRequirement> requiredShards;
            private final @NonNull String nextRankName;
            private final String consoleCommand;
        }

        private final short id;
        private final @NonNull String name;
        private final @NonNull String display;
        private final @NonNull String group;
        private final @NonNull ItemStack item;
        private final Evolution evolution;
    }

    @Getter
    private final @NonNull String defaultRank;
    private final @NonNull Map<Short, Rank> ranksById;
    private final @NonNull Map<String, Rank> ranksByName;

    public RanksData(@NonNull String defaultRank, @NonNull Collection<Rank> ranks) {
        this.defaultRank = defaultRank;
        this.ranksById = ranks.stream()
                .sorted(Comparator.comparingInt(Rank::getId))
                .collect(Collectors.toMap(Rank::getId, Function.identity(), (r1, r2) -> r1));
        this.ranksByName = ranks.stream().collect(Collectors.toMap(Rank::getName, Function.identity(), (r1, r2) -> r1));
    }

    public Rank getById(short id) {
        return ranksById.get(id);
    }

    public Rank getPrevious(short id) {
        return ranksById.entrySet().stream()
            .filter(rankEntry ->
                rankEntry.getValue().getEvolution() != null &&
                    rankEntry.getValue().getEvolution().nextRankName.equals(getById(id).name)
            )
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }

    public Rank getByName(@NonNull String name) {
        return ranksByName.get(name);
    }

    public @NonNull Collection<Rank> all() {
        return ranksById.values().immutable();
    }
}
