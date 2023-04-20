package com.worldplugins.rankup.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.ShardsData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

@ExtensionMethod({
    ConfigurationExtensions.class,
    CollectionExtensions.class
})

public class ShardsConfig implements InjectedConfigCache<ShardsData> {
    @ConfigSpec(path = "fragmentos")
    public @NonNull ShardsData transform(@NonNull FileConfiguration config) {
        return new ShardsData(
            config.map(section -> new ShardsData.Shard(
                section.getByte("Id"),
                section.getString("Nome"),
                section.getString("Display"),
                section.numberFormat("Preco"),
                section.numberFormat("Limite").intValue(),
                section.numberFormat("Limite-padrao").intValue(),
                section.getItem("Iten", false),
                section.getItem("Iten-limite", false)
            ))
        );
    }
}