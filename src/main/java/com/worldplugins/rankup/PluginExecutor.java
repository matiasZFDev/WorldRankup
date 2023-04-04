package com.worldplugins.rankup;

import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.registry.CommandRegistry;
import com.worldplugins.lib.registry.ViewRegistry;
import com.worldplugins.rankup.command.*;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import com.worldplugins.rankup.database.DatabaseManager;
import com.worldplugins.rankup.factory.NewRankupPlayerFactory;
import com.worldplugins.rankup.factory.RankupPlayerFactory;
import com.worldplugins.rankup.factory.ShardFactory;
import com.worldplugins.rankup.factory.ShardFactoryImpl;
import com.worldplugins.rankup.init.ConfigCacheInitializer;
import com.worldplugins.lib.manager.config.ConfigCacheManager;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.manager.config.YamlConfigManager;
import com.worldplugins.lib.manager.view.MenuContainerManager;
import com.worldplugins.lib.manager.view.MenuContainerManagerImpl;
import com.worldplugins.lib.manager.view.ViewManager;
import com.worldplugins.lib.manager.view.ViewManagerImpl;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.rankup.listener.RegisterOnJoinListener;
import com.worldplugins.rankup.listener.ShardInteractListener;
import com.worldplugins.rankup.listener.ShardLimitInteractListener;
import com.worldplugins.rankup.view.BagView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class PluginExecutor {
    private final @NonNull JavaPlugin plugin;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull ConfigManager configManager;
    private final @NonNull ConfigCacheManager configCacheManager;
    private final @NonNull MenuContainerManager menuContainerManager;
    private final @NonNull ViewManager viewManager;

    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull ShardFactory shardFactory;

    public PluginExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new SchedulerBuilder(plugin);
        configManager = new YamlConfigManager(plugin);
        configCacheManager = new ConfigCacheInitializer(configManager).init();
        menuContainerManager = new MenuContainerManagerImpl();
        viewManager = new ViewManagerImpl();

        databaseManager = new DatabaseManager(configManager, plugin, scheduler);
        shardFactory = new ShardFactoryImpl(
            configCacheManager.get(MainConfig.class), configCacheManager.get(ShardsConfig.class)
        );
    }

    /**
     * @return A runnable executed when disabling
     * */
    public @NonNull Runnable execute() {
        prepareGlobalAccess();
        registerListeners();
        registerCommands();
        registerViews();
        scheduleTasks();
        return () -> {
            databaseManager.getShardUpdater().update();
        };
    }

    private void prepareGlobalAccess() {
        GlobalAccess.setMessages(configCacheManager.get(MessagesConfig.class));
        GlobalAccess.setSounds(configCacheManager.get(SoundsConfig.class));
        GlobalAccess.setEffects(configCacheManager.get(EffectsConfig.class));
        GlobalAccess.setViewManager(viewManager);
    }

    private void regListeners(@NonNull Listener... listeners) {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (final Listener listener : listeners) {
            pluginManager.registerEvents(listener, plugin);
        }
    }

    private void registerListeners() {
        final RankupPlayerFactory playerFactory = new NewRankupPlayerFactory(
            configCacheManager.get(ShardsConfig.class)
        );
        final ShardsConfig shardsConfig = configCacheManager.get(ShardsConfig.class);

        regListeners(
            new RegisterOnJoinListener(databaseManager.getPlayerService(), playerFactory),
            new ShardInteractListener(shardsConfig, databaseManager.getPlayerService(), shardFactory),
            new ShardLimitInteractListener(shardsConfig, databaseManager.getPlayerService(), shardFactory)
        );
    }

    private void registerCommands() {
        final CommandRegistry registry = new CommandRegistry(plugin);
        final MainConfig mainConfig = configCacheManager.get(MainConfig.class);
        final ShardsConfig shardsConfig = configCacheManager.get(ShardsConfig.class);

        registry.command(
            new Help(),
            new Bag(databaseManager.getPlayerService()),
            new GiveShards(shardsConfig, mainConfig, shardFactory, databaseManager.getPlayerService()),
            new RemoveShards(shardsConfig, databaseManager.getPlayerService()),
            new SetShards(shardsConfig, databaseManager.getPlayerService()),
            new GiveShardLimit(shardsConfig, mainConfig, shardFactory, databaseManager.getPlayerService()),
            new RemoveShardLimit(shardsConfig, databaseManager.getPlayerService()),
            new SetShardLimit(shardsConfig, databaseManager.getPlayerService())
        );
        registry.autoTabCompleter("rankup");
        registry.registerAll();
    }

    private void registerViews() {
        final ViewRegistry registry = new ViewRegistry(viewManager, menuContainerManager, configManager);
        final ShardsConfig shardsConfig = configCacheManager.get(ShardsConfig.class);
        registry.register(
            new BagView(shardsConfig, databaseManager.getPlayerService())
        );
    }

    private void scheduleTasks() {
        final int updateSeconds = 30;
        scheduler.newTimer(() -> databaseManager.getShardUpdater().update())
            .async(false)
            .delay((long) updateSeconds * 20)
            .period((long) updateSeconds * 20)
            .run();
    }
}
