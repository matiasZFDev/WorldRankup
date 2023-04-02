package com.worldplugins.rankup;

import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.registry.CommandRegistry;
import com.worldplugins.lib.registry.ViewRegistry;
import com.worldplugins.rankup.command.Help;
import com.worldplugins.rankup.database.DatabaseManager;
import com.worldplugins.rankup.init.ConfigCacheInitializer;
import com.worldplugins.lib.manager.config.ConfigCacheManager;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.manager.config.YamlConfigManager;
import com.worldplugins.lib.manager.view.MenuContainerManager;
import com.worldplugins.lib.manager.view.MenuContainerManagerImpl;
import com.worldplugins.lib.manager.view.ViewManager;
import com.worldplugins.lib.manager.view.ViewManagerImpl;
import com.worldplugins.lib.util.SchedulerBuilder;
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

    public PluginExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new SchedulerBuilder(plugin);
        configManager = new YamlConfigManager(plugin);
        configCacheManager = new ConfigCacheInitializer(configManager).init();
        menuContainerManager = new MenuContainerManagerImpl();
        viewManager = new ViewManagerImpl();

        databaseManager = new DatabaseManager(configManager, plugin, scheduler);
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

    }

    private void registerCommands() {
        final CommandRegistry registry = new CommandRegistry(plugin);

        registry.command(
            new Help()
        );
        registry.autoTabCompleter("rankup");
        registry.registerAll();
    }

    private void registerViews() {
        final ViewRegistry registry = new ViewRegistry(viewManager, menuContainerManager, configManager);
        registry.register();
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
