package com.worldplugins.rankup;

import com.worldplugins.lib.common.ConfigCache;
import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.registry.CommandRegistry;
import com.worldplugins.lib.registry.ViewRegistry;
import com.worldplugins.lib.util.ConversationProvider;
import com.worldplugins.rankup.command.*;
import com.worldplugins.rankup.command.prestige.EvolvePrestige;
import com.worldplugins.rankup.command.prestige.RegressPrestige;
import com.worldplugins.rankup.command.prestige.SetPrestige;
import com.worldplugins.rankup.command.rank.EvolveRank;
import com.worldplugins.rankup.command.rank.RegressRank;
import com.worldplugins.rankup.command.rank.SetRank;
import com.worldplugins.rankup.command.shard.*;
import com.worldplugins.rankup.config.*;
import com.worldplugins.rankup.database.DatabaseAccessor;
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
import com.worldplugins.rankup.init.EconomyInitializer;
import com.worldplugins.rankup.init.PermissionManagerInitializer;
import com.worldplugins.rankup.listener.*;
import com.worldplugins.rankup.listener.earn.EarnExecutor;
import com.worldplugins.rankup.manager.EvolutionManager;
import com.worldplugins.rankup.view.BagView;
import com.worldplugins.rankup.view.PrestigeView;
import com.worldplugins.rankup.view.RankupView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
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

    private final @NonNull DatabaseAccessor databaseAccessor;
    private final @NonNull ShardFactory shardFactory;
    private final Economy economy;
    private final @NonNull EvolutionManager evolutionManager;

    public PluginExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new SchedulerBuilder(plugin);
        configManager = new YamlConfigManager(plugin);
        configCacheManager = new ConfigCacheInitializer(configManager).init();
        menuContainerManager = new MenuContainerManagerImpl();
        viewManager = new ViewManagerImpl();

        databaseAccessor = new DatabaseAccessor(configManager, plugin, scheduler);
        shardFactory = new ShardFactoryImpl(
            configCacheManager.get(MainConfig.class), configCacheManager.get(ShardsConfig.class)
        );
        economy = new EconomyInitializer(plugin).init();
        evolutionManager = new EvolutionManager(
            databaseAccessor.getPlayerService(), config(RanksConfig.class), config(PrestigeConfig.class),
            new PermissionManagerInitializer().init()
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
        registerPlaceholders();
        scheduleTasks();
        return () -> {
            databaseAccessor.getShardUpdater().update();
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

    private @NonNull <T> T config(Class<? extends ConfigCache<?>> clazz) {
        return configCacheManager.get(clazz);
    }

    private void registerListeners() {
        final RankupPlayerFactory playerFactory = new NewRankupPlayerFactory(
            config(RanksConfig.class), config(PrestigeConfig.class), config(ShardsConfig.class)
        );
        final EarnExecutor earnExecutor = new EarnExecutor(
            config(EarnConfig.class), shardFactory, config(ShardsConfig.class),
            databaseAccessor.getPlayerService(), config(MainConfig.class)
        );

        regListeners(
            new LoadOnJoinListener(
                databaseAccessor.getPlayerService(), playerFactory, databaseAccessor.getCacheUnloader(),
                evolutionManager
            ),
            new ShardInteractListener(
                config(ShardsConfig.class), databaseAccessor.getPlayerService(), shardFactory
            ),
            new ShardLimitInteractListener(
                config(ShardsConfig.class), databaseAccessor.getPlayerService(), shardFactory
            ),
            new ShardEarnListener(earnExecutor),
            new UnloadOnQuitListener(databaseAccessor.getCacheUnloader()),
            new PlayerTagChatListener(
                databaseAccessor.getPlayerService(), config(RanksConfig.class), config(PrestigeConfig.class)
            )
        );
    }

    private void registerCommands() {
        final CommandRegistry registry = new CommandRegistry(plugin);
        final MainConfig mainConfig = configCacheManager.get(MainConfig.class);
        final ShardsConfig shardsConfig = configCacheManager.get(ShardsConfig.class);

        registry.command(
            new Help(),
            new Bag(databaseAccessor.getPlayerService()),
            new GiveShards(shardsConfig, mainConfig, shardFactory, databaseAccessor.getPlayerService()),
            new RemoveShards(shardsConfig, databaseAccessor.getPlayerService()),
            new SetShards(shardsConfig, databaseAccessor.getPlayerService()),
            new GiveShardLimit(shardsConfig, mainConfig, shardFactory, databaseAccessor.getPlayerService()),
            new RemoveShardLimit(shardsConfig, databaseAccessor.getPlayerService()),
            new SetShardLimit(shardsConfig, databaseAccessor.getPlayerService()),
            new SetRank(evolutionManager, config(RanksConfig.class)),
            new EvolveRank(databaseAccessor.getPlayerService(), evolutionManager, config(RanksConfig.class)),
            new RegressRank(databaseAccessor.getPlayerService(), evolutionManager, config(RanksConfig.class)),
            new SetPrestige(evolutionManager, config(RanksConfig.class)),
            new EvolvePrestige(databaseAccessor.getPlayerService(), evolutionManager, config(RanksConfig.class)),
            new RegressPrestige(databaseAccessor.getPlayerService(), evolutionManager, config(RanksConfig.class)),
            new Rankup(databaseAccessor.getPlayerService(), config(RanksConfig.class)),
            new Prestige(databaseAccessor.getPlayerService()),
            new Reload(configManager, configCacheManager, menuContainerManager)
        );
        registry.autoTabCompleter("rankup");
        registry.registerAll();
    }

    private void registerViews() {
        final ViewRegistry registry = new ViewRegistry(viewManager, menuContainerManager, configManager);
        final ConversationProvider conversationProvider = new ConversationProvider(plugin);

        registry.register(
            new BagView(
                config(ShardsConfig.class), databaseAccessor.getPlayerService(), config(MainConfig.class),
                conversationProvider, economy, shardFactory
            ),
            new RankupView(
                databaseAccessor.getPlayerService(), config(RanksConfig.class), config(ShardsConfig.class),
                economy, evolutionManager
            ),
            new PrestigeView(
                databaseAccessor.getPlayerService(), config(RanksConfig.class), config(PrestigeConfig.class),
                evolutionManager
            )
        );
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return;

        new RankupPlaceholders(
            databaseAccessor.getPlayerService(), config(RanksConfig.class), config(PrestigeConfig.class)
        ).register();
    }

    private void scheduleTasks() {
        final int updateSeconds = 30;
        final int unloadSeconds = 30;
        scheduler.newTimer(databaseAccessor.getShardUpdater()::update)
            .async(false)
            .delay((long) updateSeconds * 20)
            .period((long) updateSeconds * 20)
            .run();
        scheduler.newTimer(databaseAccessor.getCacheUnloader()::unloadAll)
            .async(false)
            .delay(unloadSeconds * 20L)
            .period(unloadSeconds * 20L)
            .run();
    }
}
