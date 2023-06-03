package com.worldplugins.rankup.config.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RanksData {
    public static class Rank {
        public static class Evolution {
            public static class ShardRequirement {
                private final @NotNull String name;
                private final int amount;

                public ShardRequirement(@NotNull String name, int amount) {
                    this.name = name;
                    this.amount = amount;
                }

                public @NotNull String name() {
                    return name;
                }

                public int amount() {
                    return amount;
                }
            }

            private final double coinsPrice;
            private final @NotNull Collection<ShardRequirement> requiredShards;
            private final @NotNull String nextRankName;
            private final @NotNull List<String> consoleCommands;

            public Evolution(
                double coinsPrice,
                @NotNull Collection<ShardRequirement> requiredShards,
                @NotNull String nextRankName,
                @NotNull List<String> consoleCommands
            ) {
                this.coinsPrice = coinsPrice;
                this.requiredShards = requiredShards;
                this.nextRankName = nextRankName;
                this.consoleCommands = consoleCommands;
            }

            public double coinsPrice() {
                return coinsPrice;
            }

            public @NotNull Collection<ShardRequirement> requiredShards() {
                return requiredShards;
            }

            public @NotNull String nextRankName() {
                return nextRankName;
            }

            public @NotNull List<String> consoleCommands() {
                return consoleCommands;
            }
        }

        private final short id;
        private final @NotNull String name;
        private final @NotNull String display;
        private final @NotNull String group;
        private final @NotNull ItemStack item;
        private final @Nullable Evolution evolution;

        public Rank(
            short id,
            @NotNull String name,
            @NotNull String display,
            @NotNull String group,
            @NotNull ItemStack item,
            @Nullable Evolution evolution
        ) {
            this.id = id;
            this.name = name;
            this.display = display;
            this.group = group;
            this.item = item;
            this.evolution = evolution;
        }

        public short id() {
            return id;
        }

        public @NotNull String name() {
            return name;
        }

        public @NotNull String display() {
            return display;
        }

        public @NotNull String group() {
            return group;
        }

        public @NotNull ItemStack item() {
            return item;
        }

        public @Nullable Evolution evolution() {
            return evolution;
        }
    }

    private final @NotNull String defaultRank;
    private final @NotNull Map<Short, Rank> ranksById;
    private final @NotNull Map<String, Rank> ranksByName;

    public RanksData(@NotNull String defaultRank, @NotNull Collection<Rank> ranks) {
        this.defaultRank = defaultRank;
        this.ranksById = ranks.stream()
                .sorted(Comparator.comparingInt(Rank::id))
                .collect(Collectors.toMap(Rank::id, Function.identity(), (r1, r2) -> r1));
        this.ranksByName = ranks.stream().collect(Collectors.toMap(Rank::name, Function.identity(), (r1, r2) -> r1));
    }

    public @NotNull String defaultRank() {
        return defaultRank;
    }

    public Rank getById(short id) {
        return ranksById.get(id);
    }

    public Rank getPrevious(short id) {
        return ranksById.entrySet().stream()
            .filter(rankEntry ->
                rankEntry.getValue().evolution() != null &&
                rankEntry.getValue().evolution().nextRankName.equals(getById(id).name)
            )
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }

    public Rank getByName(@NotNull String name) {
        return ranksByName.get(name);
    }

    public @NotNull List<Rank> all() {
        return new ArrayList<>(ranksById.values());
    }
}
