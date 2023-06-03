package com.worldplugins.rankup.config;

import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.prestige.IndividualPrestiges;
import com.worldplugins.rankup.config.data.prestige.Prestige;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class PrestigeConfig implements ConfigModel<PrestigeData> {
    private @UnknownNullability PrestigeData data;
    private final @NotNull ConfigWrapper configWrapper;

    public PrestigeConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new PrestigeData(
            (short) config.getInt("Prestigio-padrao"),
            new IndividualPrestiges(
                ConfigSections.map(config.getConfigurationSection("Prestigios"), section ->
                    new Prestige(
                        (short) section.getInt("Id"),
                        section.getString("Display"),
                        section.getString("Grupo"),
                        ConfigSections.notExistingOrFalse(section, "Proximo")
                            ? null
                            : (short) section.getInt("Proximo")
                    )
                )
            )
        );
        return;
    }

    @Override
    public @NotNull PrestigeData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}