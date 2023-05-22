package com.worldplugins.rankup.config;

import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.rankup.config.data.ShardsData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import me.post.lib.util.NumberFormats;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class ShardsConfig implements ConfigModel<ShardsData> {
    private @UnknownNullability ShardsData data;
    private final @NotNull ConfigWrapper configWrapper;

    public ShardsConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new ShardsData(
            ConfigSections.map(config, section -> new ShardsData.Shard(
                (byte) section.getInt("Id"),
                section.getString("Nome"),
                section.getString("Display"),
                Double.parseDouble(NumberFormats.numerify(section.getString("Preco"))),
                Integer.parseInt(NumberFormats.numerify(section.getString("Limite"))),
                Integer.parseInt(NumberFormats.numerify(section.getString("Limite-padrao"))),
                ConfigSections.getItem(section.getConfigurationSection("Iten")),
                ConfigSections.getItem(section.getConfigurationSection("Iten-limite"))
            ))
        );
    }

    @Override
    public @NotNull ShardsData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}