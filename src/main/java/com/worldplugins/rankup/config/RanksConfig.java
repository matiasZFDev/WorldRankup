package com.worldplugins.rankup.config;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.StateConfig; import com.worldplugins.lib.config.cache.annotation.Config;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtensionMethod(value = {
    ConfigurationExtensions.class
}, suppressBaseMethods = false)

@Config(path = "ranks")
public class RanksConfig extends StateConfig<RanksConfig.Config> {

    public RanksConfig(Logger logger, @NonNull ConfigContainer configContainer) {
        super(logger, configContainer);
    }

    @RequiredArgsConstructor
    public static class Config {
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

        private Config(@NonNull String defaultRank, @NonNull Collection<Rank> ranks) {
            this.defaultRank = defaultRank;
            this.ranksById = ranks.stream().collect(Collectors.toMap(Rank::getId, Function.identity()));
            this.ranksByName = ranks.stream().collect(Collectors.toMap(Rank::getName, Function.identity()));
        }

        public Rank getById(short id) {
            return ranksById.get(id);
        }

        public Rank getPrevious(short id) {
            return ranksById.entrySet().stream()
                .filter(rankEntry ->
                    rankEntry.getValue().id == id &&
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
    }


    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        return new Config(
            config.getString("Rank-padrao"),
            config.map(section -> new Config.Rank(
                section.getByte("Id"),
                section.getString("Nome"),
                section.getString("Display"),
                section.getString("Grupo"),
                section.getItem("Iten", false),
                !section.getBoolean("Evolucao")
                    ? null
                    : new Config.Rank.Evolution(
                        section.numberFormat("Dinheiro"),
                    ((Stream<String>) section.getStringList("Fragmentos").stream())
                            .map(shardEntry -> {
                                final String[] shardData = shardEntry.split(":");
                                final String name = shardData[0];
                                final int amount = Integer.parseInt(shardData[1]);
                                return new Config.Rank.Evolution.ShardRequirement(name, amount);
                            })
                            .collect(Collectors.toList()),
                        section.getString("Seguinte"),
                        !section.getBoolean("Comando-console")
                            ? null
                            : section.getString("Comando-console")
                    )
            ))
        );
    }
}