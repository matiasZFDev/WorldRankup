package com.worldplugins.rankup.init;

import com.worldplugins.lib.database.sql.SQLDatabase;
import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.factory.SQLDatabaseFactoryProducer;
import com.worldplugins.rankup.database.dao.PlayerDAO;
import com.worldplugins.rankup.database.dao.SQLPlayerDAO;
import me.post.lib.common.Factory;
import me.post.lib.config.wrapper.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DatabaseInitializer {
    private final @NotNull ConfigManager configManager;
    private final @NotNull Plugin plugin;

    public DatabaseInitializer(@NotNull ConfigManager configManager, @NotNull Plugin plugin) {
        this.configManager = configManager;
        this.plugin = plugin;
    }

    public @NotNull PlayerDAO init() {
        return new SQLPlayerDAO(new SQLExecutor(databaseFactory().create().connect()));
    }

    private @NotNull Factory<SQLDatabase> databaseFactory() {
        final ConfigurationSection dataSection = configManager.getWrapper("config").unwrap().getConfigurationSection("Database");
        final String databaseType = dataSection.getString("Tipo");
        final Factory<SQLDatabase> sqlFactory = new SQLDatabaseFactoryProducer(databaseType, dataSection, plugin).create();

        if (sqlFactory == null) {
            throw new IllegalArgumentException("O tipo de banco de dados '" + databaseType + "' não existe.");
        }

        return sqlFactory;
    }
}
