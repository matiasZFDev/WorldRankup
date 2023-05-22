package com.worldplugins.rankup.config;

import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MainConfig implements ConfigModel<MainData> {
    private @UnknownNullability MainData data;
    private final @NotNull ConfigWrapper configWrapper;

    public MainConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    @Override
    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new MainData(
            ConfigSections.itemDisplay(config, "Display-fragmento-fisico"),
            ConfigSections.itemDisplay(config, "Display-limite-fisico"),
            fetchShardCompensations(config.getConfigurationSection("Compensacao-fragmentos")),
            fetchShardCompensations(config.getConfigurationSection("Compensacao-limite")),
            config.getBoolean("Retirar-fragmentos"),
            ConfigSections.notExistingOrFalse(config, "Venda-fragmentos")
                ? null
                : fetchSellOptions(config.getConfigurationSection("Venda-fragmentos"))
        );
    }

    @Override
    public @NotNull MainData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }

    private @NotNull EnumSet<ShardCompensation> fetchShardCompensations(@NotNull ConfigurationSection section) {
        final Set<ShardCompensation> compensations = section.getKeys(false).stream()
            .map(key -> {
                final ShardCompensation compensation = ShardCompensation.fromConfigName(key);

                if (compensation == null) {
                    throw new Error("O tipo de compensação de fragmentos '" + key + "não existe.");
                }

                return compensation;
            })
            .filter(compensation -> section.getBoolean(compensation.configName()))
            .collect(Collectors.toSet());

        return compensations.isEmpty()
            ? EnumSet.noneOf(ShardCompensation.class)
            : EnumSet.copyOf(compensations);
    }

    private @NotNull MainData.ShardSellOptions fetchSellOptions(@NotNull ConfigurationSection section) {
        final boolean useTag = section.getBoolean("Usar-tag");
        return new MainData.ShardSellOptions(
            useTag,
            useTag ? section.getString("Sem-bonus") : null,
            ConfigSections.map(section.getConfigurationSection("Bonus"), bonusSection ->
                new MainData.ShardSellOptions.SellBonus(
                    bonusSection.getString("Grupo"),
                    (byte) bonusSection.getInt("Prioridade"),
                    bonusSection.getDouble("Bonus"),
                    useTag ? bonusSection.getString("Tag") : null
                )
            )
        );
    }
}