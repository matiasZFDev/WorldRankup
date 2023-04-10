package com.worldplugins.rankup.config;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.StateConfig;
import com.worldplugins.lib.config.cache.annotation.Config;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.prestige.IndividualPrestiges;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import com.worldplugins.rankup.config.data.prestige.Prestiges;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

@ExtensionMethod({
    ConfigurationExtensions.class
})

@Config(path = "prestigio")
public class PrestigeConfig extends StateConfig<PrestigeConfig.Config> {

    public PrestigeConfig(Logger logger, @NonNull ConfigContainer configContainer) {
        super(logger, configContainer);
    }

    @RequiredArgsConstructor
    @Getter
    public static class Config {
        private final @NonNull Prestiges prestiges;
    }

    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        final String type = config.getString("Tipo");

        if (type.equals("INDIVIDUAL")) {
            return new Config(new IndividualPrestiges(
                config.getConfigurationSection("Prestigio").map(section ->
                    new Prestige(
                        section.getShort("Id"),
                        section.getString("Display"),
                        section.getString("Grupo"),
                        !section.getBoolean("Proximo")
                            ? null
                            : section.getShort("Grupo")
                    )
                )
            ));
        }

        throw new Error("O tipo de prestigio '" + type + "' não existe.");
    }
}