package com.worldplugins.rankup.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumSet;
import java.util.stream.Collectors;

@ExtensionMethod({
    ConfigurationExtensions.class,
    GenericExtensions.class
})

public class MainConfig implements InjectedConfigCache<MainData> {
    @ConfigSpec(path = "config")
    public @NonNull MainData transform(@NonNull FileConfiguration config) {
        return new MainData(
            config.itemDisplay("Display-fragmento-fisico"),
            config.itemDisplay("Display-limite-fisico"),
            fetchShardCompensations(config.getConfigurationSection("Compensacao-fragmentos")),
            fetchShardCompensations(config.getConfigurationSection("Compensacao-limite")),
            config.getBoolean("Retirar-fragmentos"),
            config.notExistingOrFalse("Venda-fragmentos")
                ? null
                : fetchSellOptions(config.getConfigurationSection("Venda-fragmentos"))
        );
    }

    private @NonNull EnumSet<ShardCompensation> fetchShardCompensations(@NonNull ConfigurationSection section) {
        return section.getKeys(false).stream()
            .map(key ->
                ShardCompensation.fromConfigName(key).orElseThrow(() ->
                    new Error("O tipo de compensação de fragmentos '" + key + "não existe.")
                )
            )
            .filter(compensation -> section.getBoolean(compensation.getConfigName()))
            .collect(Collectors.toSet())
            .use(compensations ->
                compensations.isEmpty()
                    ? EnumSet.noneOf(ShardCompensation.class)
                    : EnumSet.copyOf(compensations)
            );
    }

    private @NonNull MainData.ShardSellOptions fetchSellOptions(@NonNull ConfigurationSection section) {
        final boolean useTag = section.getBoolean("Usar-tag");
        return new MainData.ShardSellOptions(
            useTag,
            useTag ? section.getString("Sem-bonus") : null,
            section.getConfigurationSection("Bonus").map(bonusSection ->
                new MainData.ShardSellOptions.SellBonus(
                    bonusSection.getString("Grupo"),
                    bonusSection.getByte("Prioridade"),
                    bonusSection.getDouble("Bonus"),
                    useTag ? bonusSection.getString("Tag") : null
                )
            )
        );
    }
}