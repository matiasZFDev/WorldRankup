package com.worldplugins.rankup.config;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.StateConfig;
import com.worldplugins.lib.config.cache.annotation.Config;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;

@Config(path = "config")
public class MainConfig extends StateConfig<MainConfig.Config> {

    public MainConfig(Logger logger, @NonNull ConfigContainer configContainer) {
        super(logger, configContainer);
    }

    public static class Config {

    }

    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        return new Config();
    }
}