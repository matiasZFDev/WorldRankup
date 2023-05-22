package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.config.model.menu.MenuData;
import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.lib.util.MenuModels;
import me.post.lib.config.wrapper.ConfigWrapper;
import me.post.lib.util.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;

public class RanksMenuModel implements MenuModel {
    private @UnknownNullability MenuData data;
    private final @NotNull ConfigWrapper configWrapper;

    public RanksMenuModel(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        data = MenuModels.fetch(configWrapper.unwrap())
            .modifyItems(items -> items.all().forEach(item -> Items.colorMeta(item.item())))
            .getData(dataSection -> new HashMap<String, Object>() {{
                put("Slots", dataSection.getIntegerList("Slots"));
                put("Fragmento-formato", dataSection.getString("Fragmento-formato"));
                put("Iten-rank", ConfigSections.getItem(dataSection.getConfigurationSection("Iten-rank")));
                put("Iten-rank-atingido", ConfigSections.getItem(dataSection.getConfigurationSection("Iten-rank-atingido")));
            }})
            .build();
    }

    @Override
    public @NotNull MenuData data() {
        return data;
    }
}
