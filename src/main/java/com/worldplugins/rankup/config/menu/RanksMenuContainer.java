package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.cache.annotation.MenuContainerSpec;
import com.worldplugins.lib.config.cache.menu.InjectedMenuContainer;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

@ExtensionMethod({
    ConfigurationExtensions.class,
    ItemExtensions.class
})

@MenuContainerSpec(name = "ranks")
public class RanksMenuContainer implements InjectedMenuContainer {
    public @NonNull MenuData createData(@NonNull ConfigurationSection section) {
        return MenuDataUtils.fetch(section)
            .modifyItems(items -> items.all().forEach(item -> item.getItem().inPlaceColorMeta()))
            .modifyData(dataSection -> new HashMap<String, Object>() {{
                put("Slots", dataSection.getIntegerList("Slots"));
                put("Fragmento-formato", dataSection.getString("Fragmento-formato"));
                put("Iten-rank", dataSection.getItem("Iten-rank"));
                put("Iten-rank-atingido", dataSection.getItem("Iten-rank-atingido"));
            }})
            .build();
    }
}
