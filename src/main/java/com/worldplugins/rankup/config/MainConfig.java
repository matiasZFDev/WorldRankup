package com.worldplugins.rankup.config;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.StateConfig;
import com.worldplugins.lib.config.cache.annotation.Config;
import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.ShardCompensation;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.Collectors;

@ExtensionMethod({
    ConfigurationExtensions.class,
    GenericExtensions.class
})

@Config(path = "config")
public class MainConfig extends StateConfig<MainConfig.Config> {

    public MainConfig(Logger logger, @NonNull ConfigContainer configContainer) {
        super(logger, configContainer);
    }

    @RequiredArgsConstructor
    public static class Config {
        public static class ShardSellOptions {
            @RequiredArgsConstructor
            @Getter
            private static class SellBonus {
                private final @NonNull String permission;
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

            private ShardSellOptions(
                boolean useTag,
                String noGroupTag,
                @NonNull Collection<SellBonus> sellBonusList
            ) {
                this.useTag = useTag;
                this.noGroupTag = noGroupTag;
                this.sellBonusList = sellBonusList.stream()
                    .sorted(Comparator.comparingInt(SellBonus::getPriority))
                    .collect(Collectors.toList());
            }

            /**
             * @return a positive value if present, -1 if not
             * */
            public @NonNull Double getBonus(@NonNull Player player) {
                return sellBonusList.stream()
                    .filter(bonus -> player.hasPermission(bonus.getPermission()))
                    .findFirst()
                    .map(SellBonus::getBonus)
                    .orElse(-1d);
            }
        }

        @Getter
        private final @NonNull ItemDisplay shardDisplay;

        @Getter
        private final @NonNull ItemDisplay limitDisplay;

        private final @NonNull EnumSet<ShardCompensation> shardCompensations;

        private final @NonNull EnumSet<ShardCompensation> limitCompensations;

        @Getter
        private final Integer maxMerged;

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

    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        return new Config(
            config.itemDisplay("Display-fragmento-fisico"),
            config.itemDisplay("Display-limite-fisico"),
            fetchShardCompensations(config.getConfigurationSection("Compensacao-fragmentos")),
            fetchShardCompensations(config.getConfigurationSection("Compensacao-limite")),
            1000000000,
            config.getBoolean("Retirar-fragmentos"),
            !config.getBoolean("Venda-fragmentos")
                ? null
                : fetchSellOptions(config.getConfigurationSection("Venda-fragmentos"))
        );
    }

    private @NonNull EnumSet<ShardCompensation> fetchShardCompensations(@NonNull ConfigurationSection section) {
        return section.getKeys(false).stream()
            .map(key ->
                ShardCompensation.fromConfigName(key).orElseThrow(() ->
                    new Error("O tipo de compensação de fragmentos '" + key + "não existe.")
                )
            )
            .filter(compensation -> section.getBoolean(compensation.getConfigName()))
            .collect(Collectors.toSet())
            .use(compensations ->
                compensations.isEmpty()
                    ? EnumSet.noneOf(ShardCompensation.class)
                    : EnumSet.copyOf(compensations)
            );
    }

    private @NonNull Config.ShardSellOptions fetchSellOptions(@NonNull ConfigurationSection section) {
        final boolean useTag = section.getBoolean("Usar-tag");
        return new Config.ShardSellOptions(
            useTag,
            useTag ? section.getString("Sem-bonus") : null,
            section.getConfigurationSection("Bonus").map(bonusSection ->
                new Config.ShardSellOptions.SellBonus(
                    bonusSection.getString("Permissao"),
                    bonusSection.getByte("Prioridade"),
                    bonusSection.getDouble("Bonus"),
                    useTag ? bonusSection.getString("Tag") : null
                )
            )
        );
    }
}