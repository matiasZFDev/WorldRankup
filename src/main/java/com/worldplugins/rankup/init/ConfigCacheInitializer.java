package com.worldplugins.rankup.init;

import com.worldplugins.lib.common.Initializer;
import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.manager.config.ConfigCacheManager;
import com.worldplugins.lib.manager.config.ConfigCacheManagerImpl;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.registry.ConfigCacheRegistry;
import com.worldplugins.rankup.config.EarnConfig;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.RanksConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigCacheInitializer implements Initializer<ConfigCacheManager> {
    private final @NonNull ConfigManager configManager;

    @Override
    public ConfigCacheManager init() {
        final ConfigCacheManager cacheManager = new ConfigCacheManagerImpl();
        final ConfigCacheRegistry registry = new ConfigCacheRegistry(cacheManager, configManager);
        registry.register(
            MessagesConfig.class,
            SoundsConfig.class,
            EffectsConfig.class,
            MainConfig.class,
            ShardsConfig.class,
            EarnConfig.class,
            RanksConfig.class
        );
        cacheManager.update();
        return cacheManager;
    }
}
