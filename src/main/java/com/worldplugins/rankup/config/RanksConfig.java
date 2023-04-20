package com.worldplugins.rankup.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.RanksData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtensionMethod(value = {
    ConfigurationExtensions.class
}, suppressBaseMethods = false)

public class RanksConfig implements InjectedConfigCache<RanksData> {
    @ConfigSpec(path = "ranks")
    public @NonNull RanksData transform(@NonNull FileConfiguration config) {
        return new RanksData(
            config.getString("Rank-padrao"),
            config.section("Ranks").map(section -> new RanksData.Rank(
                section.getByte("Id"),
                section.getString("Nome"),
                section.getString("Display"),
                section.getString("Grupo"),
                section.getItem("Iten", false),
                section.notExistingOrFalse("Evolucao")
                    ? null
                    : new RanksData.Rank.Evolution(
                        section.numberFormat("Evolucao.Dinheiro"),
                    ((Stream<String>) section.getStringList("Evolucao.Fragmentos").stream())
                            .map(shardEntry -> {
                                final String[] shardData = shardEntry.split(":");
                                final String name = shardData[0];
                                final int amount = Integer.parseInt(shardData[1]);
                                return new RanksData.Rank.Evolution.ShardRequirement(name, amount);
                            })
                            .collect(Collectors.toList()),
                        section.getString("Evolucao.Seguinte"),
                        section.notExistingOrFalse("Evolucao.Comando-console")
                            ? null
                            : section.getString("Evolucao.Comando-console")
                    )
            ))
        );
    }
}