package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.cache.annotation.MenuContainerSpec;
import com.worldplugins.lib.config.cache.menu.InjectedMenuContainer;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;

@ExtensionMethod({
    ItemExtensions.class
})

@MenuContainerSpec(name = "prestigio")
public class PrestigeMenuContainer implements InjectedMenuContainer {
    public MenuData createData(@NonNull ConfigurationSection section) {
        return MenuDataUtils.fetch(section).build();
    }
}
