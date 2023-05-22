package com.worldplugins.rankup;

import com.worldplugins.lib.config.Updatables;
import com.worldplugins.rankup.command.*;
import com.worldplugins.rankup.command.prestige.EvolvePrestigeCommand;
import com.worldplugins.rankup.command.prestige.RegressPrestigeCommand;
import com.worldplugins.rankup.command.prestige.SetPrestigeCommand;
import com.worldplugins.rankup.command.rank.EvolveRankCommand;
import com.worldplugins.rankup.command.rank.RegressRankCommand;
import com.worldplugins.rankup.command.rank.SetRankCommand;
import com.worldplugins.rankup.command.shard.*;
import com.worldplugins.rankup.config.*;
import com.worldplugins.rankup.config.data.*;
import com.worldplugins.rankup.config.menu.BagMenuModel;
import com.worldplugins.rankup.config.menu.PrestigeMenuModel;
import com.worldplugins.rankup.config.menu.RanksMenuModel;
import com.worldplugins.rankup.config.menu.RankupMenuModel;
import com.worldplugins.rankup.database.DatabaseAccessor;
import com.worldplugins.rankup.factory.NewRankupPlayerFactory;
import com.worldplugins.rankup.factory.RankupPlayerFactory;
import com.worldplugins.rankup.factory.ShardFactory;
import com.worldplugins.rankup.factory.ShardFactoryImpl;
import com.worldplugins.rankup.init.EconomyInitializer;
import com.worldplugins.rankup.init.PermissionManagerInitializer;
import com.worldplugins.rankup.listener.*;
import com.worldplugins.rankup.listener.earn.EarnExecutor;
import com.worldplugins.rankup.manager.EvolutionManager;
import com.worldplugins.rankup.view.BagView;
import com.worldplugins.rankup.view.PrestigeView;
import com.worldplugins.rankup.view.RanksView;
import com.worldplugins.rankup.view.RankupView;
import me.post.lib.command.process.CommandRegistry;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigManager;
import me.post.lib.config.wrapper.YamlConfigManager;
import me.post.lib.util.ConversationProvider;
import me.post.lib.util.Scheduler;
import me.post.lib.view.Views;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PluginExecutor {
    private final @NotNull JavaPlugin plugin;
    private final @NotNull Scheduler scheduler;
    private final @NotNull ConfigManager configManager;
    private final @NotNull Updatables updatables;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;
    private final @NotNull ConfigModel<EarnData> earnConfig;

    private final @NotNull DatabaseAccessor databaseAccessor;
    private final @NotNull ShardFactory shardFactory;
    private final @NotNull Economy economy;
    private final @NotNull EvolutionManager evolutionManager;

    public PluginExecutor(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new Scheduler(plugin);
        configManager = new YamlConfigManager(plugin);
        loadConfiguration();

        updatables = new Updatables();
        mainConfig = updatables.include(new MainConfig(configManager.getWrapper("config")));
        ranksConfig = updatables.include(new RanksConfig(configManager.getWrapper("ranks")));
        prestigeConfig = updatables.include(new PrestigeConfig(configManager.getWrapper("prestigio")));
        shardsConfig = updatables.include(new ShardsConfig(configManager.getWrapper("fragmentos")));
        earnConfig = updatables.include(new EarnConfig(configManager.getWrapper("recompensas")));

        databaseAccessor = new DatabaseAccessor(configManager, plugin, scheduler);
        shardFactory = new ShardFactoryImpl(mainConfig, shardsConfig);
        economy = new EconomyInitializer(plugin).init();
        evolutionManager = new EvolutionManager(
            databaseAccessor.playerService(), ranksConfig, prestigeConfig, new PermissionManagerInitializer().init()
        );
    }

    private void loadConfiguration() {
        Arrays.asList(
            "config", "fragmentos", "prestigio", "ranks", "recompensas",
            "resposta/efeitos", "resposta/mensagens", "resposta/sons",
            "menu/mochila", "menu/prestigio", "menu/ranks", "menu/rankup"
        ).forEach(configManager::load);
    }

    /**
     * @return A runnable executed when disabling
     * */
    public @NotNull Runnable execute() {
        setupGlobalResponse();
        registerListeners();
        registerCommands();
        registerViews();
        registerPlaceholders();
        scheduleTasks();
        updatables.update();
        return databaseAccessor.shardUpdater()::update;
    }

    private void setupGlobalResponse() {
        Response.setup(updatables, configManager);
    }

    private void regListeners(@NotNull Listener... listeners) {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (final Listener listener : listeners) {
            pluginManager.registerEvents(listener, plugin);
        }
    }

    private void registerListeners() {
        final RankupPlayerFactory playerFactory = new NewRankupPlayerFactory(
            ranksConfig, prestigeConfig, shardsConfig
        );
        final EarnExecutor earnExecutor = new EarnExecutor(
            databaseAccessor.playerService(), shardFactory, ranksConfig, earnConfig, shardsConfig, mainConfig
        );

        regListeners(
            new LoadOnJoinListener(
                databaseAccessor.playerService(), playerFactory, databaseAccessor.cacheUnloader(),
                evolutionManager
            ),
            new ShardInteractListener(shardsConfig, databaseAccessor.playerService(), shardFactory),
            new ShardLimitInteractListener(shardsConfig, databaseAccessor.playerService(), shardFactory),
            new ShardEarnListener(earnExecutor),
            new UnloadOnQuitListener(databaseAccessor.cacheUnloader()),
            new PlayerTagChatListener(databaseAccessor.playerService(), ranksConfig, prestigeConfig)
        );
    }

    private void registerCommands() {
        final CommandRegistry registry = CommandRegistry.on(plugin);

        registry.addModules(
            new HelpCommand(),
            new BagCommand(databaseAccessor.playerService()),
            new GiveShardsCommand(shardsConfig, mainConfig, shardFactory, databaseAccessor.playerService()),
            new RemoveShardsCommand(shardsConfig, databaseAccessor.playerService()),
            new SetShardsCommand(shardsConfig, databaseAccessor.playerService()),
            new GiveShardLimitCommand(shardsConfig, mainConfig, shardFactory, databaseAccessor.playerService()),
            new RemoveShardLimit(shardsConfig, databaseAccessor.playerService()),
            new SetShardLimitCommand(shardsConfig, databaseAccessor.playerService()),
            new SetRankCommand(evolutionManager, ranksConfig),
            new EvolveRankCommand(databaseAccessor.playerService(), evolutionManager, ranksConfig),
            new RegressRankCommand(databaseAccessor.playerService(), evolutionManager, ranksConfig),
            new SetPrestigeCommand(evolutionManager, prestigeConfig),
            new EvolvePrestigeCommand(databaseAccessor.playerService(), evolutionManager, prestigeConfig),
            new RegressPrestigeCommand(databaseAccessor.playerService(), evolutionManager, prestigeConfig),
            new RankupCommand(databaseAccessor.playerService(), ranksConfig),
            new PrestigeCommand(databaseAccessor.playerService()),
            new ReloadCommand(configManager, updatables),
            new RanksCommand()
        );
        registry.registerAll();
    }

    private void registerViews() {
        final ConversationProvider conversationProvider = new ConversationProvider(plugin);

        Views.get().register(
            new BagView(
                updatables.include(new BagMenuModel(configManager.getWrapper("menu/mochila"))),
                shardsConfig, databaseAccessor.playerService(), mainConfig,conversationProvider, economy, shardFactory
            ),
            new RankupView(
                updatables.include(new RankupMenuModel(configManager.getWrapper("menu/rankup"))),
                databaseAccessor.playerService(), ranksConfig, shardsConfig, economy, evolutionManager
            ),
            new PrestigeView(
                updatables.include(new PrestigeMenuModel(configManager.getWrapper("menu/prestigio"))),
                databaseAccessor.playerService(), ranksConfig, prestigeConfig, evolutionManager
            ),
            new RanksView(
                updatables.include(new RanksMenuModel(configManager.getWrapper("menu/ranks"))),
                ranksConfig, shardsConfig, databaseAccessor.playerService()
            )
        );
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return;

        new RankupPlaceholders(databaseAccessor.playerService(), ranksConfig, prestigeConfig).register();
    }

    private void scheduleTasks() {
        final int updateSeconds = 30;
        final int unloadSeconds = 30;
        scheduler.runTimer(
            updateSeconds * 20, updateSeconds * 20, false, databaseAccessor.shardUpdater()::update
        );
        scheduler.runTimer(
            unloadSeconds * 20, unloadSeconds * 20, false, databaseAccessor.cacheUnloader()::unloadAll
        );
    }
}
