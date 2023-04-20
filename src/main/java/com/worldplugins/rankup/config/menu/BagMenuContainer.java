package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.cache.annotation.MenuContainerSpec;
import com.worldplugins.lib.config.cache.menu.InjectedMenuContainer;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

@ExtensionMethod({
    ConfigurationExtensions.class
})

@MenuContainerSpec(name = "mochila")
public class BagMenuContainer implements InjectedMenuContainer {
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
