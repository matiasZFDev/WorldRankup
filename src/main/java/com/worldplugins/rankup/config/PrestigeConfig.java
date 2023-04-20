package com.worldplugins.rankup.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.prestige.IndividualPrestiges;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

@ExtensionMethod({
    ConfigurationExtensions.class
})

public class PrestigeConfig implements InjectedConfigCache<PrestigeData> {
    @ConfigSpec(path = "prestigio")
    public @NonNull PrestigeData transform(@NonNull FileConfiguration config) {
        final String type = config.getString("Tipo");

        if (type.equals("INDIVIDUAL")) {
            return new PrestigeData(
                config.getShort("Prestigio-padrao"),
                new IndividualPrestiges(
                    config.section("Prestigios").map(section ->
                        new Prestige(
                            section.getShort("Id"),
                            section.getString("Display"),
                            section.getString("Grupo"),
                            section.notExistingOrFalse("Proximo")
                                ? null
                                : section.getShort("Proximo")
                        )
                    )
                )
            );
        }

        throw new Error("O tipo de prestigio '" + type + "' não existe.");
    }
}