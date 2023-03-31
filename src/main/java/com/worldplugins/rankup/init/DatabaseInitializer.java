package com.worldplugins.rankup.init;

import com.worldplugins.lib.common.Factory;
import com.worldplugins.lib.common.Initializer;
import com.worldplugins.lib.database.sql.SQLDatabase;
import com.worldplugins.lib.factory.SQLDatabaseFactoryProducer;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.util.SchedulerBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class DatabaseInitializer implements Initializer<Boolean> {
    private final @NonNull ConfigManager configManager;
    private final @NonNull Plugin plugin;
    private final @NonNull SchedulerBuilder schedulerBuilder;

    @Override
    public Boolean init() {
        return true;
    }

    private @NonNull Factory<SQLDatabase> databaseFactory() {
        final ConfigurationSection dataSection = configManager.getContainer("config").config().getConfigurationSection("Database");
        final String databaseType = dataSection.getString("Tipo");
        final Factory<SQLDatabase> sqlFactory = new SQLDatabaseFactoryProducer(databaseType, dataSection, plugin).create();

        if (sqlFactory == null)
            throw new IllegalArgumentException("O tipo de banco de dados '" + databaseType + "' não existe.");

        return sqlFactory;
    }
}
