package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.cache.annotation.MenuContainerSpec;
import com.worldplugins.lib.config.cache.menu.InjectedMenuContainer;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

@ExtensionMethod({
    ItemExtensions.class
})

@MenuContainerSpec(name = "rankup")
public class RankupMenuContainer implements InjectedMenuContainer {
    public MenuData createData(@NonNull ConfigurationSection section) {
        return MenuDataUtils.fetch(section)
            .modifyItems(items -> {
                items.getById("Confirmar").getItem().inPlaceColorMeta();
                items.getById("Cancelar").getItem().inPlaceColorMeta();
            })
            .modifyData(dataSection -> new HashMap<String, Object>() {{
                put("Status-suficiente", dataSection.getString("Status-suficiente"));
                put("Status-insuficiente", dataSection.getString("Status-insuficiente"));
                put("Formato-fragmento", dataSection.getString("Formato-fragmento"));
            }})
            .build();
    }
}
