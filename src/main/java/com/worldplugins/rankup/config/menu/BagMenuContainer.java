package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.annotation.MenuContainerOf;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.StateMenuContainer;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

@ExtensionMethod({
    ConfigurationExtensions.class
})

@MenuContainerOf(name = "mochila")
public class BagMenuContainer extends StateMenuContainer {
    public BagMenuContainer(Logger logger, @NonNull ConfigContainer configContainer, String section) {
        super(logger, configContainer, section);
    }

    @Override
    public MenuData createData(@NonNull ConfigurationSection section) {
        return MenuDataUtils.fetch(section)
            .modifyData(dataSection -> {
                return new HashMap<String, Object>(){{
                    put("Slots", dataSection.getIntegerList("Slots"));
                    put("Display-fragmento", dataSection.itemDisplay("Display-fragmento"));
                }};
            })
            .build();
    }
}
