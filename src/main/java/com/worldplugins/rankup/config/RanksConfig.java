package com.worldplugins.rankup.config;

import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.rankup.config.data.RanksData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import me.post.lib.util.NumberFormats;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.stream.Collectors;

public class RanksConfig implements ConfigModel<RanksData> {
    private @UnknownNullability RanksData data;
    private final @NotNull ConfigWrapper configWrapper;

    public RanksConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new RanksData(
            config.getString("Rank-padrao"),
            ConfigSections.map(config.getConfigurationSection("Ranks"), section -> new RanksData.Rank(
                (byte) section.getInt("Id"),
                section.getString("Nome"),
                section.getString("Display"),
                section.getString("Grupo"),
                ConfigSections.getItem(section.getConfigurationSection("Iten")),
                ConfigSections.notExistingOrFalse(section, "Evolucao")
                    ? null
                    : new RanksData.Rank.Evolution(
                        Double.parseDouble(NumberFormats.numerify(section.getString("Evolucao.Dinheiro"))),
                        section.getStringList("Evolucao.Fragmentos").stream()
                            .map(shardEntry -> {
                                final String[] shardData = shardEntry.split(":");
                                final String name = shardData[0];
                                final int amount = Integer.parseInt(shardData[1]);
                                return new RanksData.Rank.Evolution.ShardRequirement(name, amount);
                            })
                            .collect(Collectors.toList()),
                        section.getString("Evolucao.Seguinte"),
                        section.getStringList("Evolucao.Comandos")
                    )
            ))
        );
    }

    @Override
    public @NotNull RanksData data() {
        return data;
    }

    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}