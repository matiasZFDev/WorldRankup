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
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
        @Getter
        private final @NonNull ItemDisplay shardDisplay;
        private final @NonNull EnumSet<ShardCompensation> compensations;

        public boolean hasShardCompensation(@NonNull ShardCompensation compensation) {
            return compensations.contains(compensation);
        }
    }

    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        final ConfigurationSection shardCompensation = config.getConfigurationSection("Compensacao-fragmentos");
        return new Config(
            config.itemDisplay("Display-fragmento-fisico"),
            shardCompensation.getKeys(false).stream()
                .map(key ->
                    ShardCompensation.fromConfigName(key).orElseThrow(() ->
                        new Error("O tipo de compensação de fragmentos '" + key + "não existe.")
                    )
                )
                .filter(compensation -> shardCompensation.getBoolean(compensation.getConfigName()))
                .collect(Collectors.toSet())
                .use(compensations ->
                    compensations.isEmpty()
                        ? EnumSet.noneOf(ShardCompensation.class)
                        : EnumSet.copyOf(compensations)
                )
        );
    }
}